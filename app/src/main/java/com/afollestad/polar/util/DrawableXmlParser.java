package com.afollestad.polar.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;

import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DrawableXmlParser {

    private DrawableXmlParser() {
    }

    private static Category mCurrentCategory;

    private static ArrayList<Category> mCategories;

    public static class Icon implements Serializable {

        private String mName;
        private final String mDrawable;
        private final Category mCategory;

        public Icon(String drawable, Category category) {
            mDrawable = drawable;
            mCategory = category;
            getName(); // generate name
        }

        static final int SPACE = 1;
        static final int CAPS = 2;
        static final int CAPS_LOCK = 3;

        public long getUniqueId() {
            return mName.hashCode();
        }

        public String getName() {
            if (mName != null || mDrawable == null) return mName;

            StringBuilder sb = new StringBuilder();
            int underscoreMode = 0;
            boolean foundFirstLetter = false;
            boolean lastWasLetter = false;

            for (int i = 0; i < mDrawable.length(); i++) {
                final char c = mDrawable.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    if (underscoreMode == SPACE) {
                        sb.append(' ');
                        underscoreMode = CAPS;
                    }
                    if (!foundFirstLetter && underscoreMode == CAPS)
                        sb.append(c);
                    else sb.append(i == 0 || underscoreMode > 1 ? Character.toUpperCase(c) : c);
                    if (underscoreMode < CAPS_LOCK)
                        underscoreMode = 0;
                    foundFirstLetter = true;
                    lastWasLetter = true;
                } else if (c == '_') {
                    if (underscoreMode == CAPS_LOCK) {
                        if (lastWasLetter) {
                            underscoreMode = SPACE;
                        } else {
                            sb.append(c);
                            underscoreMode = 0;
                        }
                    } else {
                        underscoreMode++;
                    }
                    lastWasLetter = false;
                }
            }

            mName = sb.toString();
            return mName;
        }

        public Category getCategory() {
            return mCategory;
        }

        public String getDrawable() {
            return mDrawable;
        }

        public int getDrawableId(Context context) {
            if (mDrawable == null)
                return 0;
            return context.getResources().getIdentifier(mDrawable, "drawable", BuildConfig.APPLICATION_ID);
        }

        @Override
        public String toString() {
            return getDrawable();
        }
    }

    public static class Category implements Serializable {

        private final String mName;
        private final ArrayList<Icon> mIcons;

        public Category(String name) {
            mName = name;
            mIcons = new ArrayList<>();
        }

        public String getName() {
            return mName;
        }

        public List<Icon> getIcons() {
            return mIcons;
        }

        public void addItem(Icon icon) {
            mIcons.add(icon);
        }

        public int size() {
            return mIcons.size();
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "%s (%d)", mName, getIcons().size());
        }
    }

    public static List<Category> parse(@NonNull Context context, @XmlRes int xmlRes) {
        mCategories = new ArrayList<>();
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getXml(xmlRes);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        final String tagName = parser.getName();
                        if (tagName.equalsIgnoreCase("category")) {
                            mCurrentCategory = new Category(parser.getAttributeValue(null, "title"));
                            mCategories.add(mCurrentCategory);
                        } else if (tagName.equalsIgnoreCase("item")) {
                            if (mCurrentCategory == null) {
                                mCurrentCategory = new Category(context.getString(R.string.default_category));
                                mCategories.add(mCurrentCategory);
                            }
                            mCurrentCategory.addItem(new Icon(parser.getAttributeValue(null, "drawable"), mCurrentCategory));
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (parser != null)
                parser.close();
        }

        mCurrentCategory = null;
        return mCategories;
    }

    public static void cleanup() {
        mCategories = null;
    }
}