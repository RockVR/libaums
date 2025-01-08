/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package me.jahnen.libaums.core.fs.ntfs;

/**
 * A FSFile is a representation of a single block of bytes on a file system. It
 * is comparable to an inode in Unix.
 *
 * An FSFile does not have any knowledge of who is using this file. It is also
 * possible that the system uses a single FSFile instance to create two
 * inputstream's for two different principals.
 *
 * @author epr
 */
public interface FSFile extends FSObject {
}
