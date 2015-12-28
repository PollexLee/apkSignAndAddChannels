package TextAPKChannel;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.*;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

class ChangeAPKChannel extends TransferHandler {

    private JTextArea textarea;
    private JButton button;
    private static final byte[] BUFFER = new byte[4096 * 1024];
    private static String storepass = "这里放签名文件的密码";
    private static String keypass = "这里放签名的证书密码";
    public ChangeAPKChannel(JTextArea filePathList, JButton button) {
        this.textarea = filePathList;
        this.button = button;

    }

    public boolean importData(JComponent c, Transferable t) {
        try {
            List files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
            //FileReader   reader   =   new   FileReader((File)files.get(0)); 
            //textarea.read(reader,   null); 

            Iterator iterator = files.iterator();
            while (iterator.hasNext()) {
                File f = (File) iterator.next();
                if (f.isFile()) {
                    textarea.setText(f.getAbsolutePath());
                } else {
                    textarea.setText("不是标准文件");
                }
            }

            //reader.close(); 
            return true;
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        JTextArea textarea = new JTextArea(10, 30);
        JButton button = new JButton("添加渠道");
        textarea.setText("拖动要添加渠道的apk到这个窗口里面 ");
        button.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(ActionEvent e) {
                String apk_path = "";
                if (textarea.getText().equals("不是标准文件")) {
                    JOptionPane.showMessageDialog(null, "路径出错");
                } else {
                    apk_path = textarea.getText().trim();
                    if (!apk_path.substring(apk_path.length() - 3, apk_path.length()).equals("apk")) {
                        JOptionPane.showMessageDialog(null, "不是apk文件哦");
                        return;
                    }
                    try {
                        java.util.zip.ZipFile apkZipFile = new java.util.zip.ZipFile(apk_path);
                        File apkFile;
                        Enumeration<? extends java.util.zip.ZipEntry> entries = apkZipFile.entries();
                        String fileName;
                        int i = 0;
                        while (entries.hasMoreElements()) {
                            fileName = entries.nextElement().getName().trim();
                            if (fileName.contains("META-INF")) {
                                i = 1;
                                if (fileName.contains("META-INF/qwbcgchannel_")) {
                                    fileName = fileName.substring(fileName.indexOf("qwbcgchannel_") + 13, fileName.length());
                                    JOptionPane.showMessageDialog(null, "已有渠道：" + fileName);
                                    return;
                                }
                            }
                        }
                        String path = new File(".").getCanonicalPath();
                        File file = new File(path);
                        File[] listFiles = file.listFiles();
                        System.out.println(apk_path);
                        String apkName = apk_path.substring(apk_path.lastIndexOf("\\") + 1, apk_path.lastIndexOf(".apk"));
                        if (i == 0) {//签名
//                            JOptionPane.showMessageDialog(null, "此包还没有签名，签名之后才能打渠道包哦");
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                   textarea.setText("正在签名..."); 
                                }
                                
                            }).start();
                            Runtime runtime = Runtime.getRuntime();
                            String keystorePath = "";
                            Process process = null;
                            BufferedReader br = null;
                            createFiles(path + "\\out_" + apkName + "\\");
                            String newApkPath = path + "\\out_" + apkName +"\\sign_nochannel_"+apkName+".apk";
                            for (File ff : listFiles) {
                                if (ff.getName().toString().trim().contains(".keystore")) {
                                    keystorePath = ff.getPath().toString().trim();
                                    break;
                                }
                            }
                            if (keystorePath == null || keystorePath.equals("")) {
                                JOptionPane.showMessageDialog(null, "请把签名文件放在打包工具相同路径下");
                                textarea.setText("请把签名文件放在打包工具相同路径下");
                                return;
                            }
                            String cmd = "jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore " + keystorePath + " -storepass "+storepass+" -signedjar " + newApkPath + " " + apk_path + " yqq -keypass "+keypass;

                            System.out.println(cmd);
                            process = runtime.exec(cmd);
                            //两个线程输出log
                            new Thread(new StreamDrainer(process.getInputStream())).start();
                            new Thread(new StreamDrainer(process.getErrorStream())).start();
                            int exitValue = process.waitFor();
                            System.out.println("返回值：" + exitValue);
                            process.destroy();
                            if(exitValue == 0){
                                JOptionPane.showMessageDialog(null, "签名完成，正在打包...");
                                textarea.setText("已签名，正在打包...");
//                                return;
                            }else if(exitValue == 1){
                                JOptionPane.showMessageDialog(null, "签名错误，请联系@李朋");
                                textarea.setText("签名错误，请联系@李朋");
                            }
                            apkFile = new File(newApkPath);
                        }else{
                            apkFile = new File(apk_path);
                            textarea.setText("已签名，正在打包...");
//                            JOptionPane.showMessageDialog(null, "已签名");
                        }

//                        JOptionPane.showMessageDialog(null, "此包还没有添加渠道信息");
                        //能走到这一步说明没有渠道，开始签渠道包

                        //获取info文件
                        File infoFile = getFileFormParent(file, "info");
                        //获取info文件里面的包含channel_的渠道文件
                        ArrayList<File> channelFiles = getFilesFormParent(infoFile, "channel_");
                        
                        //遍历channel文件
                        for (File channelFile : channelFiles) {
                            //创建一个放签名包的文件夹
                            String channelFileName = channelFile.getName().toString().trim();
                            channelFileName = channelFileName.substring(0, channelFileName.length() - 4);
                            String channelAPKFolderName = path + "\\out_" + apkName + "\\" + channelFileName + "\\";
                            createFiles(channelAPKFolderName);
                            //读取渠道txt里的渠道数据
                            InputStreamReader read = new InputStreamReader(new FileInputStream(channelFile), "GBK");//考虑到编码格式
                            BufferedReader bufferedReader = new BufferedReader(read);
                            String lineTxt = null;
                            ArrayList<String> lines = new ArrayList<String>();
                            //将渠道读取到字符串list里来
                            while ((lineTxt = bufferedReader.readLine()) != null) {
                                if (!lineTxt.equals("")) {
                                    lines.add(lineTxt.trim());
                                }
                            }
                            //关闭
                            read.close();
                            bufferedReader.close();
                            //遍历渠道信息
                            String newAPKPath;
                            for (String channel : lines) {
                                System.out.println("下面打：" + channel + "的渠道");
                                //复制apk到文件夹下

                                newAPKPath = channelAPKFolderName + channel + ".apk";
                                System.out.println("签名包路径：" + newAPKPath);
                                File newAPKFile = createFile(newAPKPath);
                                fileChannelCopy(apkFile, newAPKFile);
                                //修改渠道创建一个渠道名的文件
                                net.lingala.zip4j.core.ZipFile newAPKZipFile;

                                newAPKZipFile = new net.lingala.zip4j.core.ZipFile(newAPKPath);

                                File METAFile = new File(path + "\\info\\META-INF\\");
                                deleteFiles(METAFile);
                                System.out.println(METAFile);
                                createFiles(path + "\\info/META-INF\\");
                                System.out.println(path + "\\info\\META-INF\\qwbcgchannel_" + channel);
                                createFile(path + "\\info\\META-INF\\qwbcgchannel_" + channel);
                                ZipParameters parameters = new ZipParameters();

                                newAPKZipFile.addFolder(METAFile, parameters);
                            }
                        }
                        JOptionPane.showMessageDialog(null, "添加渠道信息完成");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Logger.getLogger(ChangeAPKChannel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });
        textarea.setTransferHandler(new ChangeAPKChannel(textarea, button));

        JFrame f = new JFrame("<签名>拖动要添加渠道信息的apk到这个窗口里面 ");
        f.getContentPane().add(new JScrollPane(textarea), BorderLayout.CENTER);
        f.getContentPane().add(new JScrollPane(button), BorderLayout.SOUTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    //从父文件夹获取等于此名字的子文件
    private static File getFileFormParent(File file, String name) {
        File[] f = file.listFiles();
        for (File ff : f) {
            if (ff.getName().toString().equals(name)) {
                return ff;
            }
        }
        return null;
    }

    //从父文件夹获取包含此名字的子文件集合
    private static ArrayList<File> getFilesFormParent(File file, String name) {
        File[] f = file.listFiles();
        ArrayList<File> paths = new ArrayList<File>();
        String fileName;
        for (File ff : f) {
            fileName = ff.getName().toString();
            if (fileName.contains(name)) {
                paths.add(ff);
            }
        }
        return paths;
    }

    //创建一个文件夹，绝对路径
    private static File createFiles(File file) {
//        if (!file.exists()) {
        file.mkdirs();
//        }
        return file;
    }

    //创建一个文件夹，绝对路径
    private static File createFiles(String path) {
        File file = new File(path);
        return createFiles(file);
    }

    //删除一个文件夹，绝对路径
    private static boolean deleteFiles(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                for (File fffff : listFiles) {
                    deleteFiles(fffff);
                }
                file.delete();
            } else {
                file.delete();
            }
        }
        return true;
    }

    //创建一个文件,绝对路径
    private static File createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ChangeAPKChannel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file;
    }

    public static void fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class StreamDrainer implements Runnable {

    private InputStream ins;

    public StreamDrainer(InputStream ins) {
        this.ins = ins;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ins));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
