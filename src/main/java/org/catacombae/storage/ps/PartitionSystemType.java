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

package org.catacombae.storage.ps;

import org.catacombae.storage.ps.mbr.MBRHandlerFactory;
import org.catacombae.storage.ps.gpt.GPTHandlerFactory;
import org.catacombae.storage.ps.apm.APMHandlerFactory;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.catacombae.storage.ps.ebr.EBRHandlerFactory;

import static java.lang.System.getLogger;


/**
 * Defines the partition system types that the library knows of, and provides a
 * way of retrieving their handlers, if any implementations exist.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public enum PartitionSystemType {
    /** The PC legacy Master Boot Record partition system. */
    MBR(true, MBRHandlerFactory.class, "Master Boot Record"),

    /** The GUID Partition Table partition system. */
    GPT(true, GPTHandlerFactory.class, "GUID Partition Table", MBR),

    /** The Apple Partition Map partition system. */
    APM(true, APMHandlerFactory.class, "Apple Partition Map"),

    /**
     * The DOS Extended partition system, designed to work around the
     * 4 partition limit of the PC Master Boot Record partition scheme.
     */
    DOS_EXTENDED(false, EBRHandlerFactory.class, "DOS Extended");

    private static final Logger logger = getLogger(PartitionSystemType.class.getName());

    private final boolean isTopLevelCapable;
    private final String longName;
    private final PartitionSystemType[] overriddenPartitionSystems;

    private final LinkedList<Class<? extends PartitionSystemHandlerFactory>> factoryClasses = new LinkedList<>();

    PartitionSystemType(boolean pIsTopLevelCapable, String longName,
                        PartitionSystemType... overriddenPartitionSystems) {
        this.isTopLevelCapable = pIsTopLevelCapable;
        this.longName = longName;
        this.overriddenPartitionSystems = overriddenPartitionSystems;
    }

    PartitionSystemType(boolean pIsTopLevelCapable,
                        Class<? extends PartitionSystemHandlerFactory> pDefaultFactoryClass,
                        String longName, PartitionSystemType... overriddenPartitionSystems) {
        this(pIsTopLevelCapable, longName, overriddenPartitionSystems);

        this.factoryClasses.addLast(pDefaultFactoryClass);
    }

    /**
     * Tells whether this is a partition system that can be used as a top level
     * partition system. A top level partition system can be put directly onto a
     * raw, unformatted disk, unaided by an outer partition system.<br>
     * A partition system that is not capable of being a top level partition
     * system can arguably be called an embedded partition system. An example of
     * a non top capable partition system is the DOS Extended partition system.
     *
     * @return whether or not this partition system is top level capable.
     */
    public boolean isTopLevelCapable() {
        return isTopLevelCapable;
    }

    /**
     * Returns the name of this partition system type, in long form. For
     * instance "Apple Partition Map", "GUID Partition Table", etc.
     *
     * @return the name of this partition system type, in long form.
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Returns an array of partition system types that this partition system
     * overrides. For instance if there's both an MBR layout and a GPT layout
     * on a disk, the GPT layout should be preferred.
     *
     * @return an array of partition system types that this partition system
     * overrides.
     */
    public PartitionSystemType[] getOverriddenPartitionSystems() {
        return overriddenPartitionSystems;
    }

    /**
     * If an external implementor wants to register a factory class for a
     * PartitionSystemType, it calls this method. If there are no current
     * factory classes tied to this PartitionSystemType, the added class will
     * become the default factory for the type.
     *
     * @param pFactoryClass the factory class to register with this type.
     */
    public void addFactoryClass(Class<? extends PartitionSystemHandlerFactory> pFactoryClass) {
        this.factoryClasses.addLast(pFactoryClass);
    }

    /**
     * Returns all registered factory classes for this type. The first entry in
     * the list will be the default factory class.
     *
     * @return all registered factory classes for this type.
     */
    public List<Class<? extends PartitionSystemHandlerFactory>> getFactoryClasses() {
        return new ArrayList<>(factoryClasses);
    }

    /**
     * Returns a newly created factory from the type's default factory class.
     * If there is no factory classes defined for the type, <code>null</code> is
     * returned.
     *
     * @return a newly created factory from the type's default factory class.
     */
    public PartitionSystemHandlerFactory createDefaultHandlerFactory() {
        if (factoryClasses.isEmpty())
            return null;
        else {
            Class<? extends PartitionSystemHandlerFactory> factoryClass =
                    factoryClasses.getFirst();
            return createHandlerFactory(factoryClass);
        }
    }

    /**
     * Returns a newly created factory from a specified factory class.
     *
     * @param factoryClass the factory class of the new object.
     * @return a newly created factory from a specified factory class.
     */
    public static PartitionSystemHandlerFactory createHandlerFactory(Class<? extends PartitionSystemHandlerFactory> factoryClass) {
        try {
            Constructor<? extends PartitionSystemHandlerFactory> c = factoryClass.getConstructor();
            return c.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return null;
    }
}
