/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Carmen Alvarez (c@rmen.ca)
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
package org.jraf.android.cinetoday.mobile;

import org.jraf.android.util.io.IoUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestUtil {

    private TestUtil() {
        // prevent instantiation
    }

    public static String readTestResource(String filename) throws IOException {
        File resourceDir = new File("src/test/resources");
        File movieFile = new File(resourceDir, filename);
        FileInputStream is = new FileInputStream(movieFile);
        return IoUtil.readFully(is);
    }
}
