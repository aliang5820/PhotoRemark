package com.example.photoremark.poi;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Edison on 2016/2/17.
 */
public class PoiUtil {

    //保存为word
    public static void saveAsWord(String path, String content) {
        ByteArrayInputStream bais = null;
        FileOutputStream ostream = null;
        try {
            // 生成临时文件名称
            byte b[] = content.getBytes("utf-8");
            bais = new ByteArrayInputStream(b);
            POIFSFileSystem poifs = new POIFSFileSystem();
            DirectoryEntry directory = poifs.getRoot();
            DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
            ostream = new FileOutputStream(path);
            poifs.writeFilesystem(ostream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
                if (ostream != null) {
                    ostream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
