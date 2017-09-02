package kashmirr.social.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.kashmirr.social.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapDecodeException;

import java.io.File;

import it.sephiroth.android.library.picasso.Callback;
import it.sephiroth.android.library.picasso.Picasso;

/**
 * Created by PC-Comp on 4/17/2017.
 */

public class ImageLoader {

    public static void loadImage(Context context, boolean circleImage, boolean loadResource, @Nullable Object imageUrl,
                                 @Nullable int imageResource, int placeHolder, ImageView target, @Nullable final ImageLoadedCallback imageLoadedCallback) {
        if (loadResource) {
            if (circleImage) {
                Picasso.with(context).load(imageResource).fit()
                        .centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).transform(new PicassoCircleTransformation()).into(target, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(null, null);
                        }
                    }

                    @Override
                    public void onError() {
                        BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                        bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                        bitmapDecodeException.printStackTrace();
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                        }
                    }
                });
            } else {
                Picasso.with(context).load(imageResource).fit()
                        .centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).into(target, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(null, null);
                        }
                    }

                    @Override
                    public void onError() {
                        BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                        bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                        bitmapDecodeException.printStackTrace();
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                        }
                    }
                });
            }

        } else {
            if (circleImage) {
                if (imageUrl instanceof File) {
                    Picasso.with(context).load((File) imageUrl).fit()
                            .centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).transform(new PicassoCircleTransformation()).into(target, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, null);
                            }
                        }

                        @Override
                        public void onError() {
                            BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                            bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                            bitmapDecodeException.printStackTrace();
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                            }
                        }
                    });
                } else {
                    Picasso.with(context).load(imageUrl.toString()).fit()
                            .centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).transform(new PicassoCircleTransformation()).into(target, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, null);
                            }
                        }

                        @Override
                        public void onError() {
                            BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                            bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                            bitmapDecodeException.printStackTrace();
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                            }
                        }
                    });
                }
            } else {
                if (imageUrl instanceof File) {
                    Picasso.with(context).load((File) imageUrl).fit()
                            .centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).into(target, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, null);
                            }
                        }

                        @Override
                        public void onError() {
                            BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                            bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                            bitmapDecodeException.printStackTrace();
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                            }
                        }
                    });
                } else {
                    Picasso.with(context).load(imageUrl.toString()).fit().centerInside().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).into(target, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, null);
                            }
                        }

                        @Override
                        public void onError() {
                            BitmapDecodeException bitmapDecodeException = new BitmapDecodeException(0, 0);
                            bitmapDecodeException.initCause(new Throwable("Picasso error while loading the bitmap into image view"));
                            bitmapDecodeException.printStackTrace();
                            if (imageLoadedCallback != null) {
                                imageLoadedCallback.onImageLoaded(null, bitmapDecodeException);
                            }
                        }
                    });
                }

            }

        }
    }

    public static void loadImage(Context context, boolean circleTransform, boolean loadResource, @Nullable String imageUrl,
                                 @Nullable String imageResource, int placeHolder, ImageView target, @Nullable final ImageLoadedCallback imageLoadedCallback) {
        if (circleTransform) {
            if (target != null) {
                Ion.with(context).load(loadResource ? imageResource : imageUrl)
                        .withBitmap().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).transform(new CircleTransform()).intoImageView(target).setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(result, e);
                        }
                    }
                });
            } else {
                Ion.with(context).load(loadResource ? imageResource : imageUrl)
                        .withBitmap().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).transform(new CircleTransform()).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(result, e);
                        }
                    }
                });
            }
        } else {
            if (target != null) {
                Ion.with(context).load(loadResource ? imageResource : imageUrl)
                        .withBitmap().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).intoImageView(target).setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(result, e);
                        }
                    }
                });
            } else {
                Ion.with(context).load(loadResource ? imageResource : imageUrl)
                        .withBitmap().placeholder(placeHolder != 0 ? placeHolder : R.drawable.empty_place_holder).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (imageLoadedCallback != null) {
                            imageLoadedCallback.onImageLoaded(result, e);
                        }
                    }
                });
            }

        }
    }

    public interface ImageLoadedCallback<T> {
        void onImageLoaded(T result, Exception e);
    }

    public static void clearPicassoCache(Context context) {
        Picasso.with(context).clearCache();
    }

    public static void clearIonCache(Context context) {
        Ion.getDefault(context).getCache().clear();
        Ion.getDefault(context).getBitmapCache().clear();
        Ion.getDefault(context).configure().getResponseCache().clear();
    }

}
