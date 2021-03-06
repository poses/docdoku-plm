/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.postuploaders;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.StorageException;
import com.docdoku.server.viewers.utils.ScormUtil;
import com.google.common.io.ByteStreams;

import javax.ejb.EJB;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ScormPostUploaderImpl implements DocumentPostUploader {

    @EJB
    private IDataManagerLocal dataManager;

    @Override
    public boolean canProcess(final BinaryResource binaryResource) {
        try {
            InputStream binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
            return ScormUtil.isScormArchive(binaryResource.getName(), binaryContentInputStream);
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void process(final BinaryResource archiveBinaryResource) throws Exception {
        unzipScormArchive(archiveBinaryResource);
    }

    public void unzipScormArchive(BinaryResource archiveBinaryResource) {
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(dataManager.getBinaryResourceInputStream(archiveBinaryResource));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    OutputStream outputStream = null;
                    try {
                        String entryName = zipEntry.getName();
                        String subResourceVirtualPath = ScormUtil.getScormSubResourceVirtualPath(entryName);
                        outputStream = dataManager.getBinarySubResourceOutputStream(archiveBinaryResource, subResourceVirtualPath);
                        ByteStreams.copy(zipInputStream, outputStream);
                    } finally {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
