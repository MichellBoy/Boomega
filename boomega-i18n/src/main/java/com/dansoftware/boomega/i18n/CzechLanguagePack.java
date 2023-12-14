/*
 * Boomega - A modern book explorer & catalog application
 * Copyright (C) 2020-2022  Daniel Gyoerffy
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

package com.dansoftware.boomega.i18n;

import com.dansoftware.boomega.i18n.api.LanguagePack;
import com.dansoftware.boomega.util.Person;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.text.ParseException;
import java.test.RuleBasedCollator;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.dansoftware.boomega.i18n.DefaultResourceBundle.DEFAULT_RESOURCE_BUNDLE_NAME;

/**
 * An {@link CzechLanguagePack} is a {@link LanguagePack} that provides czech translation
 *
 * @author Michal Sika
 */
public class CzechLanguagePack extends LanguagePack {

    private static final Locale LOCALE = new Locale("cz", "CZ");

    public CzechLanguagePack() {
        super(LOCALE);
    }

    private Collator buildAbcCollator() {
        try {
            return new NullHandlingCollator(new ABCCollator());
        } catch (ParseException e) {
            return Collator.getInstance(LOCALE);
        }
    }

    @Nullable
    @Override
    public Person getTranslator() {
        return new Person("Sika", "Michal", "dansoftwareowner@gmail.com");
    }

    @Override
    public @NotNull ResourceBundle getValues() {
        return getBundle(DEFAULT_RESOURCE_BUNDLE_NAME);
    }

    @Override
    public @NotNull Collator getABCCollator() {
        return buildAbcCollator();
    }

    @Override
    protected boolean isRTL() {
        return false;
    }

    @Override
    public @NotNull String displayPersonName(@Nullable String firstName, @Nullable String lastName) {
        return String.format("%s %s", lastName, firstName);
    }

    private static final class ABCCollator extends RuleBasedCollator {
        ABCCollator() throws ParseException {
            super(
                    """
                            < a,A < á,Á < b,B < c,C < č,Č < d,D < ď,Ď < e,E \
                            < é,É < ě,Ě < f,F < g,G < h,H < ch,CH < i,I < í,Í < j,J \
                            < k,K < l,L < m,M < n,N < ň,Ň < o,O < ó,Ó < p,P < q,Q \
                            < r,R < ř,Ř < s,S < š,Š < t,T < ť,Ť < u,U < ů,Ů < ú,Ú \
                            < v,V < w,W < x,X < y,Y < ý,Ý < z,Z < ž,Ž\
                            """
            );
        }
    }
}
