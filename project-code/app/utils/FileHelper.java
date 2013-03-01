package utils;

import java.io.File;

public class FileHelper
{
    public static File[] dir(String path)
    {
	File currentDir = new File(path);
	File[] dirContent;
	dirContent = currentDir.listFiles();
	return dirContent;
    }
}
