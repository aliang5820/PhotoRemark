package com.example.photoremark;

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
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * 
 * @author txc
 * @version 1.0
 */
public class FileUtil {
	/**
	 * 拷贝一个文件
	 * @param context
	 * @param openfilename
	 * @param outfilename
	 * @return
	 */
	public static boolean copyFile(Context context,String openfilename,String outfilename){
		AssetManager asseets=context.getAssets();
        InputStream is=null;
        try {
               is=asseets.open(openfilename);
               
               File outFile = new File(outfilename);
               
               if(!outFile.getParentFile().exists()){
            	   outFile.getParentFile().mkdirs();
               }
               if(outFile.exists()) 
                   outFile.delete();

               OutputStream out = new FileOutputStream(outFile); 
               byte[] buf = new byte[1024];    
               int len;    
               while ((len = is.read(buf)) > 0)    
               {    
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
	 * @param assetDir
	 * @param copydir
	 * @param context
	 * @return
	 */
	public static boolean CopyAssets(String assetDir,String copydir,Context context) {
        String[] files;    
         try    
         {    
             files = context.getResources().getAssets().list(assetDir);    
         }    
         catch (IOException e1)    
         {    
             return false;    
         }    
         File mWorkingPath = new File(copydir);
         //if this directory does not exists, make one. 
         if(!mWorkingPath.exists())    
         {    
        	 mWorkingPath.mkdirs();
         }    

         for(int i = 0; i < files.length; i++)    
         {    
        	 String fileName = files[i]; 
             //we make sure file name not contains '.' to be a folder. 
             if(!fileName.contains("."))
             {
                 if(0==assetDir.length())
                 {
                     CopyAssets(fileName,copydir+fileName+"/",context);
                 }
                 else
                 {
                     CopyAssets(assetDir+"/"+fileName,copydir+fileName+"/",context);
                 }
                 continue;
             }
             copyFile(context,assetDir+"/"+fileName,mWorkingPath+"/"+fileName);       
        }
         return true;
	}
	/**
	 * 对文本文件进行写入操作
	 */
	public static boolean writeFile(String Filepath,String content){
		            //如果filePath是传递过来的参数，可以做一个后缀名称判断； 没有指定的文件名没有后缀，则自动保存为.txt格式
		 boolean flag=false; 
		 File mfile=new File(Filepath);
		 if(mfile.exists() &&mfile.length()>0)  mfile.delete();
		 File file=new File(Filepath);
		 if(!file.getParentFile().exists()){
			 file.getParentFile().mkdirs();
		 }
		            try {
		            OutputStream outstream = new FileOutputStream(file);
		            OutputStreamWriter out = new OutputStreamWriter(outstream);
		            out.write(content.toString());
		            out.close();
		            flag=true;
		            } catch (java.io.IOException e) {
		            e.printStackTrace();
		        }
		            return flag;
	}
	/**
	 * 对文本文件进行读取操作
	 */
	public static StringBuffer readFile(String path){
		StringBuffer content = new StringBuffer(); //文件内容字符串
		    //打开文件
		    File file = new File(path);
		    //如果path是传递过来的参数，可以做一个非目录的判断
		    if (file.isDirectory() || !file.isFile()){
		    	Log.v("readFile","没有指定文本文件！");
		    }
		    else{
		    try {
		    InputStream instream = new FileInputStream(file);
		    if (instream != null) {
		    InputStreamReader inputreader = new InputStreamReader(instream);
		    BufferedReader buffreader = new BufferedReader(inputreader);
		    String line;
		//分行读取
		    while (( line = buffreader.readLine()) != null) {
		    content.append(line + "\n");
		    }
		    instream.close();
		    }
		    }catch (java.io.FileNotFoundException e) {
		    	Log.v("readFile","文件不存在！");
		    } catch (IOException e) {
		         e.printStackTrace();
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
	public static boolean copyFile(String sourceFilePath, String targetFilePath){
		try {
			// 新建文件输入流并对它进行缓冲
			BufferedInputStream inBuff  = new BufferedInputStream(new FileInputStream(new File(
						sourceFilePath)));

				// 新建文件输出流并对它进行缓冲
			BufferedOutputStream outBuff = new BufferedOutputStream(new FileOutputStream(new File(
						targetFilePath)));

				// 缓冲数组
				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = inBuff.read(b)) != -1) {
					outBuff.write(b, 0, len);
				}
				// 刷新此缓冲的输出流
				outBuff.flush();
				// 关闭流
				if (inBuff != null)
					inBuff.close();
				if (outBuff != null)
					outBuff.close();
				return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
	}
	
	/**
	 * 重命名文件或文件夹
	 * 
	 * @param resFilePath
	 *            源文件路径 ,如：c:/a/b/abc.txt
	 * @param newFileName
	 *            重命名 ,如：efg.txt
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
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
	/**
	 * 拷贝目录
	 * @return
	 */
	public static boolean copyDicetroy(String oldpath,String newPath){
		 // 新建目标目录
		File nfilePath=new File(newPath);
		if(!nfilePath.exists())nfilePath.mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(oldpath)).listFiles();
        if(null!=file){
        	for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    // 源文件
                    File sourceFile = file[i];
                    // 目标文件
                    File targetFile = new File(new File(newPath).getAbsolutePath() + File.separator + file[i].getName());
                    boolean flag=copyFile(sourceFile.getPath(), targetFile.getPath());
                    if(!flag) return flag;
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
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();//递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
