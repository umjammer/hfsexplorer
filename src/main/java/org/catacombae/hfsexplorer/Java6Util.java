/*-
 * Copyright (C) 2007-2014 Erik Larsson
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

package org.catacombae.hfsexplorer;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 * This class should encapsulate all of the logic in HFSExplorer that is
 * Java 6-specific. I.E. when compiling the source code using a JDK 1.5, the
 * only class that should fail to compile would be this one.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class Java6Util extends org.catacombae.util.Java6Util {

    /**
     * Checks whether openFile can be invoked for this platform. (Internally,
     * checks whether the Java 6 operation Desktop.open(..) is supported for the
     * currently running platform.<br>
     * <b>Invoking this method on a non-Java 6 JRE will cause a class loading
     * exception.</b>
     *
     * @return whether openFile can be invoked for this platform.
     */
    public static boolean canOpen() {
        try {
            return canOpenInternal();
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
            }

            throw new RuntimeException(e);
        }
    }

    private static boolean canOpenInternal()
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException,
            NoSuchFieldException {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        Class<?> desktopActionClass = Class.forName("java.awt.Desktop$Action");

//        Desktop desktop = Desktop.getDesktop();
        Method desktopGetDesktopMethod = desktopClass.getMethod("getDesktop");
        Object desktopObject = desktopGetDesktopMethod.invoke(null);

//        Desktop.Action openAction = Desktop.Action.OPEN);
        Field desktopActionOpenField = desktopActionClass.getField("OPEN");
        Object openActionObject = desktopActionOpenField.get(null);

//        return desktop.isSupported(openAction);
        Method desktopIsSupportedMethod = desktopClass.getMethod("isSupported", desktopActionClass);
        Object returnObject = desktopIsSupportedMethod.invoke(desktopObject, openActionObject);
        if (!(returnObject instanceof Boolean)) {
            throw new RuntimeException("Unexpected type returned from " +
                    "java.awt.Desktop.isSupported(java.awt.Desktop.Action): " + returnObject.getClass());
        }

        return ((Boolean) returnObject).booleanValue();
    }

    /**
     * Sends an OS signal via Java6's Desktop.open() method to open the
     * specified file with its default handler.<br>
     * <b>Invoking this method on a non-Java 6 JRE will cause a class loading
     * exception.</b>
     *
     * @param f the file to open.
     * @throws java.io.IOException if the file could not be opened.
     */
    public static void openFile(File f) throws IOException {
        try {
            openFileInternal(f);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
            }

            throw new RuntimeException(e);
        }
    }

    private static void openFileInternal(File f)
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException,
            NoSuchFieldException {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");

//        Desktop desktop = Desktop.getDesktop();
        Method desktopGetDesktopMethod = desktopClass.getMethod("getDesktop");
        Object desktopObject = desktopGetDesktopMethod.invoke(null);

//        desktop.openFile(f);
        Method desktopOpenFileMethod = desktopClass.getMethod("open", File.class);
        desktopOpenFileMethod.invoke(desktopObject, f);
    }

    /**
     * Checks whether browse can be invoked for this platform. (Internally,
     * checks whether the Java 6 operation java.awt.Desktop.browse(..) is
     * supported for the currently running platform.<br>
     * <b>Invoking this method on a non-Java 6 JRE will cause a class loading
     * exception.</b>
     *
     * @return whether browse can be invoked for this platform.
     */
    public static boolean canBrowse() {
        try {
            return canBrowseInternal();
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
            }

            throw new RuntimeException(e);
        }
    }

    private static boolean canBrowseInternal()
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException,
            NoSuchFieldException {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        Class<?> desktopActionClass = Class.forName("java.awt.Desktop$Action");

//        Desktop desktop = Desktop.getDesktop();
        Method desktopGetDesktopMethod = desktopClass.getMethod("getDesktop");
        Object desktopObject = desktopGetDesktopMethod.invoke(null);

//        Desktop.Action browseAction = Desktop.Action.BROWSE);
        Field desktopActionOpenField = desktopActionClass.getField("BROWSE");
        Object browseActionObject = desktopActionOpenField.get(null);

//        return desktop.isSupported(browseAction);
        Method desktopIsSupportedMethod = desktopClass.getMethod("isSupported", desktopActionClass);
        Object returnObject = desktopIsSupportedMethod.invoke(desktopObject, browseActionObject);
        if (!(returnObject instanceof Boolean)) {
            throw new RuntimeException("Unexpected type returned from " +
                    "java.awt.Desktop.isSupported(java.awt.Desktop.Action): " + returnObject.getClass());
        }

        return ((Boolean) returnObject).booleanValue();
    }

    /**
     * Sends an OS signal via Java 6's java.awt.Desktop.browse() method to open
     * the specified URL with the default web browser.<br>
     * <b>Invoking this method on a non-Java 6 JRE will cause a class loading
     * exception.</b>
     *
     * @param uri the URI to browse.
     * @throws java.io.IOException if the URL could not be browsed.
     */
    public static void browse(URI uri) throws IOException {
        try {
            browseInternal(uri);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
            }

            throw new RuntimeException(e);
        }
    }

    private static void browseInternal(URI uri)
            throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException,
            NoSuchFieldException {
        Class<?> desktopClass = Class.forName("java.awt.Desktop");

//        Desktop desktop = Desktop.getDesktop();
        Method desktopGetDesktopMethod = desktopClass.getMethod("getDesktop");
        Object desktopObject = desktopGetDesktopMethod.invoke(null);

//        desktop.browse(uri);
        Method desktopOpenFileMethod = desktopClass.getMethod("browse", URI.class);
        desktopOpenFileMethod.invoke(desktopObject, uri);
    }

    /**
     * Sets the icon images for the specified Window. Java 6 supports icon
     * images of multiple sizes to better adapt across platforms.<br>
     * <b>Invoking this method on a non-Java 6 JRE will cause a class loading
     * exception.</b>
     *
     * @param icons  the different sizes of icon images that should be displayed
     *               for the window.
     * @param window the window that the icons should be applied to.
     */
    public static void setIconImages(ImageIcon[] icons, Window window) {
        LinkedList<Image> iconImages = new LinkedList<Image>();
        for (ImageIcon ii : icons)
            iconImages.addLast(ii.getImage());

        try {
            Class<? extends Window> c = window.getClass();
            Method m = c.getMethod("setIconImages", List.class);
            m.invoke(window, iconImages);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Adds a row sorter to <code>table</code> with the specified table model. Optionally, a list of
     * Comparators can be supplied, one for each column, that specify the correct way of comparing
     * the objects in that column. Null values means the default comparator will be used.<br>
     * <b>Only Java 6+ virtual machines will support this, so check first with isJava6OrHigher() or
     * risk to crash your program.</b>
     *
     * @param table
     * @param tableModel
     * @param defaultSortColumn the column on which to sort on by default.
     * @param columnComparators
     */
    public static void addRowSorter(JTable table, DefaultTableModel tableModel,
                                    int defaultSortColumn, List<Comparator<?>> columnComparators) {

        try {
            final Class<? extends Object> rowSorterClass =
                    Class.forName("javax.swing.RowSorter");
            final Class<? extends Object> tableRowSorterClass =
                    Class.forName("javax.swing.table.TableRowSorter");
            final Method tableRowSorterSetComparatorMethod =
                    tableRowSorterClass.getMethod("setComparator", int.class, Comparator.class);
            final Method tableRowSorterToggleSortOrderMethod =
                    tableRowSorterClass.getMethod("toggleSortOrder", int.class);
            final Object sorter = tableRowSorterClass.getConstructor(TableModel.class).newInstance(tableModel);

            int i = 0;
            for (Comparator<?> c : columnComparators) {
                if (c != null) {
                    tableRowSorterSetComparatorMethod.invoke(sorter, i, c);
                }

                ++i;
            }

            tableRowSorterToggleSortOrderMethod.invoke(sorter,
                    defaultSortColumn);

            Class<? extends JTable> c = table.getClass();
            Method m = c.getMethod("setRowSorter", rowSorterClass);
            m.invoke(table, sorter);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}
