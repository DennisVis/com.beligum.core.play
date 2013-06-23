package com.beligum.core.utils;

public class FileJson
{

    private Boolean isDirectory;
    private Long size;
    private String data;
    private String absolutePath;
    private Boolean isHidden;

    public Boolean getIsDirectory()
    {
	return isDirectory;
    }
    public void setIsDirectory(Boolean isDirectory)
    {
	this.isDirectory = isDirectory;
    }
    public Long getSize()
    {
	return size;
    }
    public void setSize(Long size)
    {
	this.size = size;
    }
    public String getData()
    {
	return data;
    }
    public void setData(String shortName)
    {
	this.data = shortName;
    }
    public String getAbsolutePath()
    {
	return absolutePath;
    }
    public void setAbsolutePath(String absolutePath)
    {
	this.absolutePath = absolutePath;
    }
    public Boolean getIsHidden()
    {
	return isHidden;
    }
    public void setIsHidden(Boolean isHidden)
    {
	this.isHidden = isHidden;
    }

}
