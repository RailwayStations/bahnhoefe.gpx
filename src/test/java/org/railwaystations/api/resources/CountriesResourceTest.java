package org.railwaystations.api.resources;

import org.junit.Test;
import org.mockito.Mockito;
import org.railwaystations.api.BahnhoefeRepository;
import org.railwaystations.api.loader.BahnhoefeLoader;
import org.railwaystations.api.model.Country;
import org.railwaystations.api.monitoring.LoggingMonitor;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CountriesResourceTest {

    @Test
    public void testList() throws IOException {
        final BahnhoefeLoader loaderXY = Mockito.mock(BahnhoefeLoader.class);
        Mockito.when(loaderXY.getCountry()).thenReturn(new Country("xy", "nameXY", "emailXY", "twitterXY", "timetableXY"));

        final BahnhoefeLoader loaderAB = Mockito.mock(BahnhoefeLoader.class);
        Mockito.when(loaderAB.getCountry()).thenReturn(new Country("ab", "nameAB", "emailAB", "twitterAB", "timetableAB"));

        final CountriesResource resource = new CountriesResource(new BahnhoefeRepository(new LoggingMonitor(), loaderAB, loaderXY));

        final Set<Country> countries = resource.list();
        assertThat(countries.size(), equalTo(2));
        countries.stream().forEach(this::assertCountry);
    }

    private void assertCountry(final Country country) {
        assertThat(country.getName(), equalTo("name" + country.getCode().toUpperCase()));
        assertThat(country.getEmail(), equalTo("email" + country.getCode().toUpperCase()));
        assertThat(country.getTwitterTags(), equalTo("twitter" + country.getCode().toUpperCase()));
        assertThat(country.getTimetableUrlTemplate(), equalTo("timetable" + country.getCode().toUpperCase()));
    }

}
