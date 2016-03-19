/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.mobile.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import org.jraf.android.cinetoday.R;

public class CirclePageIndicator extends View implements ViewPager.OnPageChangeListener {
    private static final float SHRINK_FACTOR = .33f;
    private ViewPager mViewPager;

    private int mCircleRadiusPx;
    private int mCircleMarginPx;
    private Paint mPaint;
    private int mPosition;
    private float mPositionOffset;
    private Bitmap mAddBitmap;

    public CirclePageIndicator(Context context) {
        super(context);
        init();
    }

    public CirclePageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CirclePageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CirclePageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCircleRadiusPx = getResources().getDimensionPixelSize(R.dimen.pageIndicator_circleRadius);
        mCircleMarginPx = getResources().getDimensionPixelSize(R.dimen.pageIndicator_circleMargin);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.pageIndicator_circle, null));

        mAddBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_page_indicator_add);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition = position;
        mPositionOffset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager) return;
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("viewPager must have an adapter");
        }
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mViewPager == null) return;
        int count = mViewPager.getAdapter().getCount();
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            float x = i * (mCircleRadiusPx * 2 + mCircleMarginPx) + mCircleRadiusPx;
            float y = mCircleRadiusPx;
            float radius;
            if (i == mPosition) {
                radius = mCircleRadiusPx - mCircleRadiusPx * SHRINK_FACTOR * mPositionOffset;
            } else if (i == mPosition + 1) {
                radius = mCircleRadiusPx - mCircleRadiusPx * SHRINK_FACTOR * (1f - mPositionOffset);
            } else {
                radius = mCircleRadiusPx - mCircleRadiusPx * SHRINK_FACTOR;
            }

            if (i == count - 1) {
                // Draw a '+'
                RectF dst = new RectF();
                dst.left = x - radius;
                dst.right = x + radius;
                dst.top = y - radius;
                dst.bottom = y + radius;
                canvas.drawBitmap(mAddBitmap, null, dst, mPaint);
            } else {
                // Draw a circle
                canvas.drawCircle(x, y, radius, mPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mViewPager == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        int count = mViewPager.getAdapter().getCount();
        setMeasuredDimension(count * (mCircleRadiusPx * 2 + mCircleMarginPx), mCircleRadiusPx * 2);
    }
}
