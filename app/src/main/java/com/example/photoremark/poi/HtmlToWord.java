package com.example.photoremark.poi;

import android.os.Environment;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Edison on 2016/2/17.
 */
public class HtmlToWord {
    private static String path = Environment.getExternalStorageDirectory().getPath() + File.separator;

    public static String getContent() {
        String content = "<html>" +
                "<head>你好</head>" +
                "<body>" +
                "<table>" +
                "<tr>" +
                "<td>信息1</td>" +
                "<td>信息2</td>" +
                "<td>t3</td>" +
                "<tr>" +
                "</table>" +
                "<form name=\"form1\" >\n" +
                "你是否喜欢旅游？请选择：<br>\n" +
                "<input type=\"radio\" name=\"radiobutton\" value=\"1\" checked> 喜欢\n" +
                "<input type=\"radio\" name=\"radiobutton\" value=\"2\"> 不喜欢\n" +
                "<input type=\"radio\" name=\"radiobutton\" value=\"3\"> 无所谓<br>\n" +
                "</form><br>\n" +
                "您对那些运动感兴趣，请选择：<br>\n" +
                "<input type=\"checkbox\" name=\"checkbox1\" value=\"1\"> 跑步\n" +
                "<input type=\"checkbox\" name=\"checkbox2\" value=\"2\"> 打球\n" +
                "<input type=\"checkbox\" name=\"checkbox3\" value=\"3\"> 登山\n" +
                "<input type=\"checkbox\" name=\"checkbox4\" value=\"4\"> 健美<br>\n" +
                "</form>" +
                "</body>" +
                "</html>";
        return content;
    }

    //保存为word
    public static void saveAsWord(String content) {
        try {
            if (!"".equals(path)) {
                // 检查目录是否存在
                File fileDir = new File(path);
                if (fileDir.exists()) {

                    // 生成临时文件名称
                    String fileName = "html.doc";
                    byte b[] = content.getBytes("gbk");
                    ByteArrayInputStream bais = new ByteArrayInputStream(b);
                    POIFSFileSystem poifs = new POIFSFileSystem();
                    DirectoryEntry directory = poifs.getRoot();
                    DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
                    FileOutputStream ostream = new FileOutputStream(path + fileName);
                    poifs.writeFilesystem(ostream);
                    bais.close();
                    ostream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String content = getContent();
        saveAsWord(content);
    }

}
