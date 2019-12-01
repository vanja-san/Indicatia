package stevekung.mods.indicatia.utils;

import stevekung.mods.indicatia.gui.GuiNumberField;

public interface ITradeGUI
{
    void onAutocompleteResponse(String[] list);
    GuiNumberField getNumberField();
}