package com.example.photoremark;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author txc
 * @version 1.0
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getName();

    /**
     * 拷贝一个文件
     *
     * @param context
     * @param openfilename
     * @param outfilename
     * @return
     */
    public static boolean copyFile(Context context, String openfilename, String outfilename) {
        AssetManager asseets = context.getAssets();
        InputStream is = null;
        try {
            is = asseets.open(openfilename);

            File outFile = new File(outfilename);

            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (outFile.exists())
                outFile.delete();

            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            is.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 拷贝目录到指定目录
     *
     * @param assetDir
     * @param copydir
     * @param context
     * @return
     */
    public static boolean CopyAssets(String assetDir, String copydir, Context context) {
        String[] files;
        try {
            files = context.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            return false;
        }
        File mWorkingPath = new File(copydir);
        //if this directory does not exists, make one.
        if (!mWorkingPath.exists()) {
            mWorkingPath.mkdirs();
        }

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            //we make sure file name not contains '.' to be a folder.
            if (!fileName.contains(".")) {
                if (0 == assetDir.length()) {
                    CopyAssets(fileName, copydir + fileName + "/", context);
                } else {
                    CopyAssets(assetDir + "/" + fileName, copydir + fileName + "/", context);
                }
                continue;
            }
            copyFile(context, assetDir + "/" + fileName, mWorkingPath + "/" + fileName);
        }
        return true;
    }

    /**
     * 对文本文件进行写入操作
     */
    public static boolean writeFile(String filePath, String content) {
        Log.e(TAG, "writeFile:" + filePath);
        Log.e(TAG, "writeFileContent:" + content);
        //如果filePath是传递过来的参数，可以做一个后缀名称判断； 没有指定的文件名没有后缀，则自动保存为.txt格式
        boolean flag = false;
        File mfile = new File(filePath);
        if (mfile.exists() && mfile.length() > 0) {
            mfile.delete();
        }
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            /*outstream = new FileOutputStream(file);
            out = new OutputStreamWriter(outstream);
            out.write(content.toString());*/
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 对文本文件进行读取操作
     */
    public static StringBuffer readFile(String path) {
        Log.e(TAG, "readFile:" + path);
        StringBuffer content = new StringBuffer(); //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory() || !file.isFile()) {
            Log.v("readFile", "没有指定文本文件！");
        } else {
            InputStream instream = null;
            InputStreamReader inputreader = null;
            BufferedReader buffreader = null;
            try {
                instream = new FileInputStream(file);
                inputreader = new InputStreamReader(instream);
                buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    content.append(line + "\n");
                }
                instream.close();
            } catch (java.io.FileNotFoundException e) {
                Log.v("readFile", "文件不存在！");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (instream != null) {
                        instream.close();
                    }
                    if (inputreader != null) {
                        inputreader.close();
                    }
                    if (buffreader != null) {
                        buffreader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * 复制文件
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @throws IOException
     * @date 2012-11-27 上午10:50:30
     */
    public static boolean copyFile(String sourceFilePath, String targetFilePath) {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(new File(
                    sourceFilePath)));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(new File(
                    targetFilePath)));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                // 关闭流
                if (inBuff != null)
                    inBuff.close();
                if (outBuff != null)
                    outBuff.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 重命名文件或文件夹
     *
     * @param resFilePath 源文件路径 ,如：c:/a/b/abc.txt
     * @param newFileName 重命名 ,如：efg.txt
     * @return 操作成功标识
     */
    public static boolean renameFile(String resFilePath, String newFileName) {
        String newFilePath = resFilePath.substring(0,
                resFilePath.lastIndexOf(File.separator) + 1)
                + newFileName;
        File resFile = new File(resFilePath);
        File newFile = new File(newFilePath);
        return resFile.renameTo(newFile);
    }

    public static boolean deleteFile(String sPath) {
        Log.e(TAG, "deleteFile:" + sPath);
        boolean flag = false;
        try {
            File file = new File(sPath);
            // 路径为文件且不为空则进行删除
            if (file.isFile() && file.exists()) {
                file.delete();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 拷贝目录
     *
     * @return
     */
    public static boolean copyDicetroy(String oldpath, String newPath) {
        // 新建目标目录
        File nfilePath = new File(newPath);
        if (!nfilePath.exists()) nfilePath.mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(oldpath)).listFiles();
        if (null != file) {
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    // 源文件
                    File sourceFile = file[i];
                    // 目标文件
                    File targetFile = new File(new File(newPath).getAbsolutePath() + File.separator + file[i].getName());
                    boolean flag = copyFile(sourceFile.getPath(), targetFile.getPath());
                    if (!flag) return flag;
                }
                if (file[i].isDirectory()) {
                    // 准备复制的源文件夹
                    String dir1 = oldpath + "/" + file[i].getName();
                    // 准备复制的目标文件夹
                    String dir2 = newPath + "/" + file[i].getName();
                    copyDicetroy(dir1, dir2);
                }
            }
        }
        return true;

    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();//递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 获取assets中的文件数据
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getStringFromAssets(Context context, String fileName) {
        String result = "";
        InputStream in = null;
        try {
            in = context.getAssets().open(fileName);  //获得AssetManger 对象, 调用其open 方法取得  对应的inputStream对象
            int size = in.available();//取得数据流的数据大小
            byte[] buffer = new byte[size];
            in.read(buffer);
            result = new String(buffer, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
