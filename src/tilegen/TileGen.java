package tilegen;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
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
            return;
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

        if(files.any()){
            Log.info("Processing @ file@...", files.size, files.size == 1 ? "" : "s");

            try{
                InputStream stream = TileGen.class.getClassLoader().getResourceAsStream("layout.png");
                enforce(stream != null, "The layout image doesn't exist.");

                ByteSeq bytes = new ByteSeq(9192);

                int next;
                while((next = stream.read()) != -1) bytes.add((byte)next);

                Pixmap layout = new Pixmap(bytes.toArray());
                enforce(layout.width == 384, "Layout's width != 384");
                enforce(layout.height == 128, "Layout's height != 128");

                for(Fi file : files){
                    try{
                        Pixmap image = new Pixmap(file);
                        enforce(image.width % 4f == 0f, "@: Image dimension must be divisible by 4");
                        enforce(image.width == image.height, "@: Image canvas must be square!", file);

                        IntMap<PixmapRegion> palettes = new IntMap<>();
                        for(int x = 0; x < 4; x++){
                            for(int y = 0; y < 4; y++){
                                palettes.put(layout.getRaw(
                                    x * layout.width / 12,
                                    y * layout.height / 4
                                ), new PixmapRegion(
                                    image,
                                    x * image.width / 4, y * image.height / 4,
                                    image.width / 4, image.height / 4
                                ));
                            }
                        }

                        Pixmap out = new Pixmap(image.width / 4 * 12, image.height);

                        for(int x = 0; x < out.width; x++){
                            for(int y = 0; y < out.height; y++){
                                PixmapRegion palette = palettes.get(layout.getRaw(
                                    x * layout.width / out.width,
                                    y * layout.height / out.height
                                ));

                                if(palette != null){
                                    out.setRaw(x, y, palette.get(
                                        x % (out.width / 12),
                                        y % (out.height / 4)
                                    ));
                                }
                            }
                        }

                        file.sibling(file.nameWithoutExtension() + "-tiled.png").writePng(out);
                        out.dispose();
                    }catch(Exception e){
                        Log.err(Strings.getFinalMessage(e));
                    }
                }

                layout.dispose();
            }catch(Exception e){
                error(e);
            }

            Log.info("Finished!");
        }
    }

    public static void enforce(boolean cond, String format, Object... args){
        if(!cond) throw new IllegalArgumentException(Strings.format(format, args));
    }

    public static void error(Throwable t){
        throw new RuntimeException(Strings.getFinalCause(t));
    }
}
