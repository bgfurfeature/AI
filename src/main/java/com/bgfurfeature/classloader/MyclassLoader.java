package com.neo.classloader;

import java.io.*;

/**
 * 双亲委派机制
 *
 * 原理：
 * 双亲委派模型的工作流程是：如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，
 * 而是把请求委托给父加载器去完成，依次向上，
 * 因此，所有的类加载请求最终都应该被传递到顶层的启动类加载器中，
 * 只有当父加载器在它的搜索范围中没有找到所需的类时，
 * 即无法完成该加载，子加载器才会尝试自己去加载该类。
 * 解析：
 1、当AppClassLoader加载一个class时，它首先不会自己去尝试加载这个类，
    而是把类加载请求委派给父类加载器ExtClassLoader去完成。

 2、当ExtClassLoader加载一个class时，它首先也不会自己去尝试加载这个类，
    而是把类加载请求委派给BootStrapClassLoader去完成。

 3、如果BootStrapClassLoader加载失败（例如在$JAVA_HOME/jre/lib里未查找到该class），
    会使用ExtClassLoader来尝试加载；

 4、若ExtClassLoader也加载失败，则会使用AppClassLoader来加载，如果AppClassLoader也加载失败，
    则会报出异常ClassNotFoundException。
 */
public class MyClassLoader extends ClassLoader {

    private String root;

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = loadClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] loadClassData(String className) {
        String fileName = root + File.separatorChar
                + className.replace('.', File.separatorChar) + ".class";
        try {
            InputStream ins = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            while ((length = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public static void main(String[] args)  {

        MyClassLoader classLoader = new MyClassLoader();
        classLoader.setRoot("E:\\temp");

        Class<?> testClass = null;
        try {
            testClass = classLoader.loadClass("com.neo.classloader.Test2");
            Object object = testClass.newInstance();
            System.out.println(object.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

/** 知识点
 *
 * JVM类加载机制： 类加载的过程包括了加载、验证、准备、解析、初始化五个阶段
 *
 • 全盘负责，当一个类加载器负责加载某个Class时，该Class所依赖的和引用的其他Class也将由该类加载器负责载入，
    除非显示使用另外一个类加载器来载入

 • 父类委托，先让父类加载器试图加载该类，只有在父类加载器无法加载该类时才尝试从自己的类路径中加载该类

 • 缓存机制，缓存机制将会保证所有加载过的Class都会被缓存，当程序中需要使用某个Class时，类加载器先从缓存区寻找该Class，
    只有缓存区不存在，系统才会读取该类对应的二进制数据，并将其转换成Class对象，存入缓存区。
    这就是为什么修改了Class后，必须重启JVM，程序的修改才会生效
 *
 */