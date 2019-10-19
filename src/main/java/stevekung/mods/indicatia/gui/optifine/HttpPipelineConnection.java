package stevekung.mods.indicatia.gui.optifine;

import java.io.*;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class HttpPipelineConnection
{
    public String host = null;
    public int port = 0;
    public Proxy proxy = Proxy.NO_PROXY;
    public List<HttpPipelineRequest> listRequests = new LinkedList();
    public List<HttpPipelineRequest> listRequestsSend = new LinkedList();
    public Socket socket = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    public HttpPipelineSender httpPipelineSender = null;
    public HttpPipelineReceiver httpPipelineReceiver = null;
    public int countRequests = 0;
    public boolean responseReceived = false;
    public long keepaliveTimeoutMs = 5000L;
    public int keepaliveMaxCount = 1000;
    public long timeLastActivityMs = System.currentTimeMillis();
    public boolean terminated = false;
    public static final String LF = "\n";
    public static final int TIMEOUT_CONNECT_MS = 5000;
    public static final int TIMEOUT_READ_MS = 5000;
    public static final Pattern patternFullUrl = Pattern.compile("^[a-zA-Z]+://.*");

    public HttpPipelineConnection(String host, int port)
    {
        this(host, port, Proxy.NO_PROXY);
    }

    public HttpPipelineConnection(String host, int port, Proxy proxy)
    {
        this.host = host;
        this.port = port;
        this.proxy = proxy;

        this.httpPipelineSender = new HttpPipelineSender(this);
        this.httpPipelineSender.start();

        this.httpPipelineReceiver = new HttpPipelineReceiver(this);
        this.httpPipelineReceiver.start();
    }

    public synchronized boolean addRequest(HttpPipelineRequest pr)
    {
        if (this.isClosed()) {
            return false;
        }
        this.addRequest(pr, this.listRequests);
        this.addRequest(pr, this.listRequestsSend);

        this.countRequests += 1;

        return true;
    }

    public void addRequest(HttpPipelineRequest pr, List<HttpPipelineRequest> list)
    {
        list.add(pr);

        this.notifyAll();
    }

    public synchronized void setSocket(Socket s)
            throws IOException
    {
        if (this.terminated) {
            return;
        }
        if (this.socket != null) {
            throw new IllegalArgumentException("Already connected");
        }
        this.socket = s;

        this.socket.setTcpNoDelay(true);

        this.inputStream = this.socket.getInputStream();

        this.outputStream = new BufferedOutputStream(this.socket.getOutputStream());

        this.onActivity();

        this.notifyAll();
    }

    public synchronized OutputStream getOutputStream()
            throws IOException, InterruptedException
    {
        while (this.outputStream == null)
        {
            this.checkTimeout();
            this.wait(1000L);
        }
        return this.outputStream;
    }

    public synchronized InputStream getInputStream()
            throws IOException, InterruptedException
    {
        while (this.inputStream == null)
        {
            this.checkTimeout();
            this.wait(1000L);
        }
        return this.inputStream;
    }

    public synchronized HttpPipelineRequest getNextRequestSend()
            throws InterruptedException, IOException
    {
        if (this.listRequestsSend.size() <= 0 && this.outputStream != null) {
            this.outputStream.flush();
        }
        return this.getNextRequest(this.listRequestsSend, true);
    }

    public synchronized HttpPipelineRequest getNextRequestReceive()
            throws InterruptedException
    {
        return this.getNextRequest(this.listRequests, false);
    }

    public HttpPipelineRequest getNextRequest(List<HttpPipelineRequest> list, boolean remove)
            throws InterruptedException
    {
        while (list.size() <= 0)
        {
            this.checkTimeout();
            this.wait(1000L);
        }
        this.onActivity();
        if (remove) {
            return list.remove(0);
        }
        return list.get(0);
    }

    public void checkTimeout()
    {
        if (this.socket == null) {
            return;
        }
        long timeoutMs = this.keepaliveTimeoutMs;
        if (this.listRequests.size() > 0) {
            timeoutMs = 5000L;
        }
        long timeNowMs = System.currentTimeMillis();
        if (timeNowMs > this.timeLastActivityMs + timeoutMs) {
            this.terminate(new InterruptedException("Timeout " + timeoutMs));
        }
    }

    public void onActivity()
    {
        this.timeLastActivityMs = System.currentTimeMillis();
    }

    public synchronized void onRequestSent(HttpPipelineRequest pr)
    {
        if (this.terminated) {
            return;
        }
        this.onActivity();
    }

    public synchronized void onResponseReceived(HttpPipelineRequest pr, HttpResponse resp)
    {
        if (this.terminated) {
            return;
        }
        this.responseReceived = true;
        this.onActivity();
        if (this.listRequests.size() <= 0 || this.listRequests.get(0) != pr) {
            throw new IllegalArgumentException("Response out of order: " + pr);
        }
        this.listRequests.remove(0);

        pr.setClosed(true);

        String location = resp.getHeader("Location");
        if (resp.getStatus() / 100 == 3 && location != null && pr.getHttpRequest().getRedirects() < 5)
        {
            try
            {
                location = this.normalizeUrl(location, pr.getHttpRequest());
                HttpRequest hr2 = HttpPipeline.makeRequest(location, pr.getHttpRequest().getProxy());
                hr2.setRedirects(pr.getHttpRequest().getRedirects() + 1);
                HttpPipelineRequest hpr2 = new HttpPipelineRequest(hr2, pr.getHttpListener());
                HttpPipeline.addRequest(hpr2);
            }
            catch (IOException e)
            {
                pr.getHttpListener().failed(pr.getHttpRequest(), e);
            }
        }
        else
        {
            HttpListener listener = pr.getHttpListener();

            listener.finished(pr.getHttpRequest(), resp);
        }
        this.checkResponseHeader(resp);
    }

    public String normalizeUrl(String url, HttpRequest hr)
    {
        if (patternFullUrl.matcher(url).matches()) {
            return url;
        }
        if (url.startsWith("//")) {
            return "http:" + url;
        }
        String server = hr.getHost();
        if (hr.getPort() != 80) {
            server = server + ":" + hr.getPort();
        }
        if (url.startsWith("/")) {
            return "http://" + server + url;
        }
        String file = hr.getFile();
        int pos = file.lastIndexOf("/");
        if (pos >= 0) {
            return "http://" + server + file.substring(0, pos + 1) + url;
        }
        return "http://" + server + "/" + url;
    }

    public void checkResponseHeader(HttpResponse resp)
    {
        String connStr = resp.getHeader("Connection");
        if (connStr != null) {
            if (!connStr.toLowerCase().equals("keep-alive")) {
                this.terminate(new EOFException("Connection not keep-alive"));
            }
        }
        String keepAliveStr = resp.getHeader("Keep-Alive");
        if (keepAliveStr != null)
        {
            String[] parts = tokenize(keepAliveStr, ",;");
            for (String part : parts)
            {
                String[] tokens = this.split(part, '=');
                if (tokens.length >= 2)
                {
                    if (tokens[0].equals("timeout"))
                    {
                        int timeout = parseInt(tokens[1], -1);
                        if (timeout > 0) {
                            this.keepaliveTimeoutMs = timeout * 1000;
                        }
                    }
                    if (tokens[0].equals("max"))
                    {
                        int max = parseInt(tokens[1], -1);
                        if (max > 0) {
                            this.keepaliveMaxCount = max;
                        }
                    }
                }
            }
        }
    }

    public String[] split(String str, char separator)
    {
        int pos = str.indexOf(separator);
        if (pos < 0) {
            return new String[] { str };
        }
        String str1 = str.substring(0, pos);
        String str2 = str.substring(pos + 1);

        return new String[] { str1, str2 };
    }

    public synchronized void onExceptionSend(HttpPipelineRequest pr, Exception e)
    {
        this.terminate(e);
    }

    public synchronized void onExceptionReceive(HttpPipelineRequest pr, Exception e)
    {
        this.terminate(e);
    }

    public synchronized void terminate(Exception e)
    {
        if (this.terminated) {
            return;
        }
        this.terminated = true;

        this.terminateRequests(e);
        if (this.httpPipelineSender != null) {
            this.httpPipelineSender.interrupt();
        }
        if (this.httpPipelineReceiver != null) {
            this.httpPipelineReceiver.interrupt();
        }
        try
        {
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch (IOException localIOException) {}
        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
    }

    public void terminateRequests(Exception e)
    {
        if (this.listRequests.size() <= 0) {
            return;
        }
        if (!this.responseReceived)
        {
            HttpPipelineRequest pr = this.listRequests.remove(0);
            pr.getHttpListener().failed(pr.getHttpRequest(), e);
            pr.setClosed(true);
        }
        while (this.listRequests.size() > 0)
        {
            HttpPipelineRequest pr = this.listRequests.remove(0);
            HttpPipeline.addRequest(pr);
        }
    }

    public synchronized boolean isClosed()
    {
        if (this.terminated) {
            return true;
        }
        if (this.countRequests >= this.keepaliveMaxCount) {
            return true;
        }
        return false;
    }

    public int getCountRequests()
    {
        return this.countRequests;
    }

    public synchronized boolean hasActiveRequests()
    {
        if (this.listRequests.size() > 0) {
            return true;
        }
        return false;
    }

    public String getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public Proxy getProxy()
    {
        return this.proxy;
    }

    public static String[] tokenize(String str, String delim)
    {
        StringTokenizer tok = new StringTokenizer(str, delim);
        List<String> list = new ArrayList<>();
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken();
            list.add(token);
        }
        String[] strs = list.toArray(new String[list.size()]);
        return strs;
    }

    public static int parseInt(String str, int defVal)
    {
        try
        {
            if (str == null) {
                return defVal;
            }
            str = str.trim();

            return Integer.parseInt(str);
        }
        catch (NumberFormatException e) {}
        return defVal;
    }
}
