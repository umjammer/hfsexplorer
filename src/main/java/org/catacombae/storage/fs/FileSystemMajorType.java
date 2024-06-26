/*-
 * Copyright (C) 2008 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.storage.fs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.catacombae.storage.fs.hfs.HFSFileSystemHandlerFactory;
import org.catacombae.storage.fs.hfsplus.HFSPlusFileSystemHandlerFactory;
import org.catacombae.storage.fs.hfsx.HFSXFileSystemHandlerFactory;

import static java.lang.System.getLogger;


/**
 * Contains the possible "major file system types" that the library will work
 * with, and defines all file system handler implementations for these file
 * systems that are available in the build.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public enum FileSystemMajorType {
    APPLE_MFS,
    APPLE_HFS(HFSFileSystemHandlerFactory.class),
    APPLE_HFS_PLUS(HFSPlusFileSystemHandlerFactory.class),
    APPLE_HFSX(HFSXFileSystemHandlerFactory.class),
    APPLE_UFS, APPLE_PRODOS, FAT12, FAT16, FAT32, EXFAT, NTFS, HPFS, EXT3,
    REISERFS, REISER4, XFS, JFS, ZFS, UNKNOWN;

    private static final Logger logger = getLogger(FileSystemMajorType.class.getName());

//    private static final Map<String, List<Class<? extends FileSystemHandlerFactory>>> DEFINED_FACTORIES;
//
//    static {
//        DEFINED_FACTORIES = new HashMap<String, List<Class<? extends FileSystemHandlerFactory>>>();
//
//        LinkedList<Class<? extends FileSystemHandlerFactory>> hfsPlusHandlers =
//                new LinkedList<Class<? extends FileSystemHandlerFactory>>();
//        hfsPlusHandlers.addLast(HFSPlusFileSystemHandlerFactory.class);
//        DEFINED_FACTORIES.put("HFS+", hfsPlusHandlers);
//    }
//
//    public static Class[] getFactories(String fsType) {
//        List<Class<? extends FileSystemHandlerFactory>> factoryList =
//                DEFINED_FACTORIES.get(fsType);
//        return factoryList.toArray(Class[]::new);
//    }

    private final LinkedList<Class<? extends FileSystemHandlerFactory>> factoryClasses = new LinkedList<>();

    /**
     * Creates a FileSystemMajorType with no file system handler implementations
     * specified.
     */
    FileSystemMajorType() {
    }

    /**
     * Creates a FileSystemMajorType specifing the factory classes of the available
     * file system handler implementations for this file system. The first argument
     * is assumed to be the default implementation.
     *
     * @param factoryClass
     */
    FileSystemMajorType(Class<? extends FileSystemHandlerFactory> factoryClass) {
        this.factoryClasses.add(factoryClass);
    }

    /**
     * If an external implementor wants to register a factory class for a type,
     * it calls this method. If there are no current factory classes tied to
     * this type, the added class will become its default factory.
     *
     * @param factoryClass the factory class to register with this type.
     */
    public void addFactoryClass(Class<? extends FileSystemHandlerFactory> factoryClass) {
        this.factoryClasses.addLast(factoryClass);
    }

    /**
     * Returns all registered factory classes for this type. The first entry in
     * the list will be the default factory class.
     *
     * @return all registered factory classes for this type.
     */
    public List<Class<? extends FileSystemHandlerFactory>> getFactoryClasses() {
        return new ArrayList<>(factoryClasses);
    }

    /**
     * Returns a newly created factory from the type's default factory class.
     * If there is no factory classes defined for the type, <code>null</code> is
     * returned.
     *
     * @return a newly created factory from the type's default factory class.
     */
    public FileSystemHandlerFactory createDefaultHandlerFactory() {
        if (factoryClasses.isEmpty())
            return null;
        else {
            Class<? extends FileSystemHandlerFactory> factoryClass = factoryClasses.getFirst();
            return createHandlerFactory(factoryClass);
        }
    }

    /**
     * Returns a newly created factory from a specified factory class.
     *
     * @param factoryClass the factory class of the new object.
     * @return a newly created factory from a specified factory class.
     */
    public static FileSystemHandlerFactory createHandlerFactory(Class<? extends FileSystemHandlerFactory> factoryClass) {
        try {
            Constructor<? extends FileSystemHandlerFactory> c = factoryClass.getConstructor();
            return c.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return null;
    }
}
