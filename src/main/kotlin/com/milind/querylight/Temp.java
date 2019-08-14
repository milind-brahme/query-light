package com.milind.querylight;


import java.awt.datatransfer.*;
import java.io.IOException;

public class Temp {
    Transferable t = new Transferable() {
        @Override
        public DataFlavor[] getTransferDataFlavors() {


            return new DataFlavor[0];
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
            return null;
        }
    };
    ClipboardOwner c = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable transferable) {

        }
    };
}
