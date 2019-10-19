package stevekung.mods.indicatia.gui.optifine;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

public class HttpPipelineSender
extends Thread
{
    public HttpPipelineConnection httpPipelineConnection = null;
    public static final String CRLF = "\r\n";
    public static Charset ASCII = Charset.forName("ASCII");

    public HttpPipelineSender(HttpPipelineConnection httpPipelineConnection)
    {
        super("HttpPipelineSender");

        this.httpPipelineConnection = httpPipelineConnection;
    }

    @Override
    public void run()
    {
        HttpPipelineRequest hpr = null;
        try
        {
            this.connect();
            while (!Thread.interrupted())
            {
                hpr = this.httpPipelineConnection.getNextRequestSend();

                HttpRequest req = hpr.getHttpRequest();

                OutputStream out = this.httpPipelineConnection.getOutputStream();
                this.writeRequest(req, out);

                this.httpPipelineConnection.onRequestSent(hpr);
            }
        }
        catch (InterruptedException e) {}catch (Exception e)
        {
            this.httpPipelineConnection.onExceptionSend(hpr, e);
        }
    }

    public void connect()
            throws IOException
    {
        String host = this.httpPipelineConnection.getHost();
        int port = this.httpPipelineConnection.getPort();

        Proxy proxy = this.httpPipelineConnection.getProxy();
        Socket socket = new Socket(proxy);
        socket.connect(new InetSocketAddress(host, port), 5000);

        this.httpPipelineConnection.setSocket(socket);
    }

    public void writeRequest(HttpRequest req, OutputStream out)
            throws IOException
    {
        this.write(out, req.getMethod() + " " + req.getFile() + " " + req.getHttp() + "\r\n");

        Map<String, String> headers = req.getHeaders();
        Set<String> keySet = headers.keySet();
        for (Object element : keySet)
        {
            String key = (String)element;
            String val = req.getHeaders().get(key);
            this.write(out, key + ": " + val + "\r\n");
        }
        this.write(out, "\r\n");
    }

    public void write(OutputStream out, String str)
            throws IOException
    {
        byte[] bytes = str.getBytes(ASCII);
        out.write(bytes);
    }
}
