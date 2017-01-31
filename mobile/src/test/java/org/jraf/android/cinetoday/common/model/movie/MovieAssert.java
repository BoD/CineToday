/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Carmen Alvarez (c@rmen.ca)
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
package org.jraf.android.cinetoday.common.model.movie;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.fest.assertions.api.AbstractAssert;
import org.fest.assertions.api.Assertions;
import org.fest.util.Sets;

public class MovieAssert extends AbstractAssert<MovieAssert, Movie> {

    public MovieAssert(Movie actual) {
        super(actual, MovieAssert.class);
    }

    private static final Set<String> REQUIRED_SHOWTIME_FIELDS =
            Sets.newLinkedHashSet("todayShowtimes");
    private static final Set<String> OPTIONAL_MOVIE_FIELDS =
            Sets.newLinkedHashSet("actors", "directors", "releaseDate", "trailerUri");
    private static final Set<String> REQUIRED_MOVIE_DETAIL_FIELDS =
            Sets.newLinkedHashSet("id", "originalTitle", "localTitle",
                    "durationSeconds", "genres", "webUri", "synopsis");
    private static final Set<String> REQUIRED_MOVIE_LIST_FIELDS =
            Sets.newLinkedHashSet("id", "localTitle", "durationSeconds", "genres", "webUri", "todayShowtimes");

    public static MovieAssert assertThat(Movie actual) {
        return new MovieAssert(actual);
    }

    public MovieAssert hasId(String id) {
        isNotNull();
        Assertions.assertThat(actual.id).isEqualTo(id);
        return this;
    }

    public MovieAssert hasOriginalTitle(String originalTitle) {
        isNotNull();
        Assertions.assertThat(actual.originalTitle).isEqualTo(originalTitle);
        return this;
    }

    public MovieAssert hasLocalTitle(String localTitle) {
        isNotNull();
        Assertions.assertThat(actual.localTitle).isEqualTo(localTitle);
        return this;
    }

    public MovieAssert hasDirectors(String directors) {
        isNotNull();
        Assertions.assertThat(actual.directors).isEqualTo(directors);
        return this;
    }

    public MovieAssert hasActors(String actors) {
        isNotNull();
        Assertions.assertThat(actual.actors).isEqualTo(actors);
        return this;
    }

    public MovieAssert hasReleaseDate(long releaseDate) {
        isNotNull();

        Assertions.assertThat(actual.releaseDate).isNotNull().hasTime(releaseDate);
        return this;
    }

    public MovieAssert hasDurationSeconds(Integer durationSeconds) {
        Assertions.assertThat(actual.durationSeconds).isEqualTo(durationSeconds);
        return this;
    }

    public MovieAssert hasPosterUri(String posterUri) {
        isNotNull();
        Assertions.assertThat(actual.posterUri).isEqualTo(posterUri);
        return this;
    }

    public MovieAssert hasTrailerUri(String trailerUri) {
        isNotNull();
        Assertions.assertThat(actual.trailerUri).isEqualTo(trailerUri);
        return this;
    }

    public MovieAssert hasWebUri(String webUri) {
        isNotNull();
        Assertions.assertThat(actual.webUri).isEqualTo(webUri);
        return this;
    }

    public MovieAssert hasSynopsis(String synopsis) {
        isNotNull();
        Assertions.assertThat(actual.synopsis).isEqualTo(synopsis);
        return this;
    }

    private MovieAssert assertClassDefinitionUpToDate() {
        Collection<String> expectedFieldNames = new TreeSet<>();
        expectedFieldNames.addAll(REQUIRED_MOVIE_DETAIL_FIELDS);
        expectedFieldNames.addAll(OPTIONAL_MOVIE_FIELDS);
        expectedFieldNames.addAll(REQUIRED_SHOWTIME_FIELDS);

        Field[] actualFields = Movie.class.getDeclaredFields();
        Collection<String> actualFieldNames = new TreeSet<>();
        for (Field field : actualFields) {
            if (!Modifier.isStatic(field.getModifiers())) actualFieldNames.add(field.getName());
        }
        Assertions.assertThat(actualFieldNames).
                isEqualTo(expectedFieldNames);
        return this;
    }

    private MovieAssert hasRequiredFields(Collection<String> fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = Movie.class.getDeclaredField(fieldName);
                Assertions.assertThat(field.get(actual))
                        .overridingErrorMessage("movie is missing the required %s field", field.getName())
                        .isNotNull();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
        return this;
    }

    // Not used yet.  Will it be useful to test that a Movie has a
    // specific list of fields?  (No more, no less)
    private MovieAssert hasOnlyRequiredFields(Collection<String> fieldNames) {
        assertClassDefinitionUpToDate();
        try {
            for (Field field : Movie.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    // This field is required and should be not null
                    if (fieldNames.contains(field.getName())) {
                        Assertions.assertThat(field.get(actual))
                                .overridingErrorMessage("movie is missing the required %s field", field.getName())
                                .isNotNull();
                    }
                    // This field isn't required and should be null
                    else {
                        Assertions.assertThat(field.get(actual))
                                .overridingErrorMessage("movie has an unexpected value %s for %s (expected null)",
                                        field.get(actual),
                                        field.getName())
                                .isNull();

                    }
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        return this;

    }

    public MovieAssert hasRequiredMovieDetailFields() {
        return hasRequiredFields(REQUIRED_MOVIE_DETAIL_FIELDS);
    }

    public MovieAssert hasRequiredMovieListFields() {
        return hasRequiredFields(REQUIRED_MOVIE_LIST_FIELDS);
    }
}
