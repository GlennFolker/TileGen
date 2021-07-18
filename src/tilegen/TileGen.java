package tilegen;

import arc.files.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;

import java.io.*;

public class TileGen{
    public static void main(String[] args){
        if(System.getProperty("tilegen.loadlogger", "true").equals("true")){
            Log.logger = (level, text) -> {
                String label;
                switch(level){
                    case info: label = "I"; break;
                    case warn: label = "W"; break;
                    case err: label = "E"; break;
                    case debug: label = "D"; break;
                    default: label = "";
                }

                System.out.println(label.isEmpty() ? text : ("[" + label + "] " + text));
            };
        }

        if(args.length == 0){
            Log.info("No images given.");
            //return;
        }

        Seq<Fi> files = new Seq<>();
        for(String arg : args){
            Fi file = Fi.get(arg);
            if(file.exists()){
                files.add(file);
            }else{
                Log.warn("'@' does not exist!", file);
            }
        }

        //if(files.any()){
            Log.info("Processing @ file@...", files.size, files.size == 1 ? "" : "s");

            try{
                InputStream stream = TileGen.class.getClassLoader().getResourceAsStream("layout.png");
                enforce(stream != null, "The layout image doesn't exist.");

                ByteSeq bytes = new ByteSeq(9192);

                int next;
                while((next = stream.read()) != -1) bytes.add((byte)next);

                Pixmap layout = new Pixmap(bytes.toArray());
                enforce(layout.getWidth() == 384, "Layout's width != 384");
                enforce(layout.getHeight() == 128, "Layout's height != 128");
            }catch(Exception e){
                throw new RuntimeException(Strings.getFinalCause(e));
            }
        //}
    }

    public static void enforce(boolean cond, String message){
        if(!cond) throw new IllegalArgumentException(message);
    }
}
