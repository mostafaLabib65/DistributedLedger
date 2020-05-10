package files;

import java.io.File;
import java.net.URL;

public class FileLoader {
    public File getFile(String resource){
        URL url = getClass().getResource(resource);
        return new File(url.getPath());
    }
}
