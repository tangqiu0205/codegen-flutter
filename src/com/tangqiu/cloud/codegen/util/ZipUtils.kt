package com.tangqiu.cloud.codegen.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object FileZipUtil {
    /**
     * 文件压缩
     * @param needZipPath 压缩文件夹路径
     * @param out         压缩文件输出流
     */
    @Throws(RuntimeException::class)
    fun fileToZip(needZipPath: String?, out: OutputStream?) {
        val start = System.currentTimeMillis() //计算压缩所需要的时间
        var zos: ZipOutputStream? = null
        try {
            zos = ZipOutputStream(out)
            val sourceFile = File(needZipPath) //获取要压缩的文件
            doFileToZip(sourceFile, zos, sourceFile.name) //开始压缩
            val end = System.currentTimeMillis()
            val useTime = end - start //压缩耗费的时间
            System.err.println("本次压缩，耗时" + useTime + "ms")
        } catch (e: Exception) {
            System.err.println("压缩失败了，呵呵 - " + e.message)
        } finally {
            if (zos != null) {
                try {
                    zos.close()
                    needZipPath?.let { delDir(needZipPath) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 递归压缩方法
     * @param sourceFile   源文件
     * @param zos         zip输出流
     * @param name        压缩后的名称
     */
    @Throws(Exception::class)
    private fun doFileToZip(sourceFile: File, zos: ZipOutputStream, name: String) {
        val buf = ByteArray(1024 * 2)
        if (sourceFile.isFile) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(ZipEntry(name))
            // copy文件到zip输出流中
            var len: Int
            val `in` = FileInputStream(sourceFile)
            while (`in`.read(buf).also { len = it } != -1) {
                zos.write(buf, 0, len)
            }
            zos.closeEntry()
            `in`.close()
        } else {
            val listFiles = sourceFile.listFiles()
            if (listFiles == null || listFiles.isEmpty()) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                zos.putNextEntry(ZipEntry("$name/")) // 空文件夹的处理
                zos.closeEntry() // 没有文件，不需要文件的copy
            } else {
                for (file in listFiles) {
                    // 判断是否需要保留原来的文件结构
                    // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                    // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                    doFileToZip(file, zos, name + "/" + file.name)
                }
            }
        }
    }

    /**
     * 删除文件夹
     * dirPath 文件路径
     */
    fun delDir(dirPath: String) {
        val dir = File(dirPath)
        deleteDirWightFile(dir)
        println("删除${dirPath}文件夹-> 成功")
    }

    private fun deleteDirWightFile(dir: File?) {
        if (dir!!.checkFile())
            return
        for (file in dir.listFiles()) {
            if (file.isFile)
                file.delete() // 删除所有文件
            else if (file.isDirectory)
                deleteDirWightFile(file) // 递归的方式删除文件夹
        }
        dir.delete()// 删除目录本身
    }

    private fun File.checkFile(): Boolean {
        return !this.exists() || !this.isDirectory
    }
}