/*******************************************************************************
 * Copyright (c) 2013 by Beligum b.v.b.a. (http://www.beligum.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Beligum - initial implementation
 *******************************************************************************/
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
