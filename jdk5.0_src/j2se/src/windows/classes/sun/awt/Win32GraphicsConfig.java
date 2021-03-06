/*
 * @(#)Win32GraphicsConfig.java	1.46 04/05/18
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;

import sun.awt.windows.Win32OffScreenSurfaceData;
import sun.awt.windows.Win32SurfaceData;
import sun.awt.windows.WinBackBuffer;
import sun.awt.windows.WComponentPeer;
import sun.awt.image.OffScreenImage;
import sun.awt.image.SunVolatileImage;
import sun.java2d.SurfaceData;
import sun.java2d.InvalidPipeException;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.CompositeType;

/**
 * This is an implementation of a GraphicsConfiguration object for a
 * single Win32 visual.
 *
 * @see GraphicsEnvironment
 * @see GraphicsDevice
 * @version 10 Feb 1997
 */
public class Win32GraphicsConfig extends GraphicsConfiguration
    implements DisplayChangedListener
{
    protected Win32GraphicsDevice screen;
    protected int visual;  //PixelFormatID
    protected RenderLoops solidloops;
    private static BufferCapabilities bufferCaps;
    private static ImageCapabilities imageCaps;    

    private static native void initIDs();

    static {
        initIDs();
    }
	
    /**
     * Returns a Win32GraphicsConfiguration object with the given device
     * and PixelFormat.  Note that this method does NOT check to ensure that
     * the returned Win32GraphicsConfig will correctly support rendering into a
     * Java window.  This method is provided so that client code can do its
     * own checking as to the appropriateness of a particular PixelFormat.  
     * Safer access to Win32GraphicsConfigurations is provided by 
     * Win32GraphicsDevice.getConfigurations().
     */
    public static Win32GraphicsConfig getConfig(Win32GraphicsDevice device,
                                                int pixFormatID)
    {
        return new Win32GraphicsConfig(device, pixFormatID);	
    }

    /**
     * @deprecated as of JDK version 1.3
     * replaced by <code>getConfig()</code>
     */
    @Deprecated
    public Win32GraphicsConfig(GraphicsDevice device, int visualnum) {
	this.screen = (Win32GraphicsDevice)device;
	this.visual = visualnum;
	((Win32GraphicsDevice)device).addDisplayChangedListener(this);
    }

    /**
     * Return the graphics device associated with this configuration.
     */
    public GraphicsDevice getDevice() {
	return screen;
    }

    /**
     * Return the PixelFormatIndex this GraphicsConfig uses
     */
    public int getVisual() {
	return visual;
    }

    /**
     * Return the RenderLoops this type of destination uses for
     * solid fills and strokes.
     */
    private SurfaceType sTypeOrig = null;
    public synchronized RenderLoops getSolidLoops(SurfaceType stype) {
	if (solidloops == null || sTypeOrig != stype) {
	    solidloops = SurfaceData.makeRenderLoops(SurfaceType.OpaqueColor,
						     CompositeType.SrcNoEa,
						     stype);
	    sTypeOrig = stype;
	}
	return solidloops;
    }

    /**
     * Returns a BufferedImage with raster and color model
     * compatible with this graphics configuration.  This method
     * has nothing to do with memory-mapping a device.  This BufferedImage
     * has a raster and color model that is closest to this native device
     * configuration and thus can be optimally blitted to this device.
     */
    public BufferedImage createCompatibleImage(int width, int height) {
	ColorModel model = getColorModel();
	WritableRaster raster =
            model.createCompatibleWritableRaster(width, height);
        return new BufferedImage(model, raster,
                                 model.isAlphaPremultiplied(), null);
    }

    /**
     * Returns a VolatileImage
     * compatible with this graphics configuration.
     * The returned <code>VolatileImage</code> has
     * a layout and color model that is closest to this native device
     * and may have data that is stored on 
     * the device (i.e., in VRAM) and can therefore be rendered to and 
     * blitted from using hardware-acceleration techniques (i.e., DirectX).
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height) {
	return new SunVolatileImage(this, width, height);
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height,
						       int transparency) 
    {
	return new SunVolatileImage(this, width, height, transparency);
    }

    /**
     * Returns a BufferedImage that supports the specified transparency
     * and has a channel layout and color model
     * compatible with this graphics configuration.  This method
     * has nothing to do with memory-mapping
     * a device. This BufferedImage has a layout and
     * color model that can be optimally blitted to a device
     * with this configuration.
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    public BufferedImage createCompatibleImage(int width, int height,
					       int transparency)
    {
        switch (transparency) {
        case Transparency.OPAQUE:
	    return createCompatibleImage(width, height);

        case Transparency.BITMASK:
        case Transparency.TRANSLUCENT:
            ColorModel cm = getColorModel(transparency);
	    WritableRaster wr = cm.createCompatibleWritableRaster(width,
                                                                  height);
            return new BufferedImage(cm, wr, 
                                     cm.isAlphaPremultiplied(), null);

        default:
            throw new IllegalArgumentException("Unknown transparency type "+
                                               transparency);
        }
    }

    /**
     * Returns the color model associated with this configuration.
     */
    public synchronized ColorModel getColorModel() {
	return screen.getColorModel();
    }

    /**
     * Returns a new color model for this configuration.  This call 
     * is only used internally, by images and components that are
     * associated with the graphics device.  When attributes of that
     * device change (for example, when the device palette is updated),
     * then this device-based color model will be updated internally
     * to reflect the new situation.
     */
    public ColorModel getDeviceColorModel() {
	return screen.getDynamicColorModel();
    }

    /**
     * Returns the color model associated with this configuration that
     * supports the specified transparency.
     */
    public ColorModel getColorModel(int transparency) {
 	switch (transparency) {
 	case Transparency.OPAQUE:
	    return getColorModel();
 	case Transparency.BITMASK:
	    return new DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
 	case Transparency.TRANSLUCENT:
	    return getTranslucentColorModel();
 	default:
	    return null;
	}
    }

    private static final int DCM_4444_RED_MASK = 0x0f00;
    private static final int DCM_4444_GRN_MASK = 0x00f0;
    private static final int DCM_4444_BLU_MASK = 0x000f;
    private static final int DCM_4444_ALP_MASK = 0xf000;
    static ColorModel translucentCM = null;
    public static ColorModel getTranslucentColorModel() {
	if (WindowsFlags.getD3DTexBpp() == 16) {
	    if (translucentCM == null) {
		translucentCM = new DirectColorModel(16,
						     DCM_4444_RED_MASK,
						     DCM_4444_GRN_MASK,
						     DCM_4444_BLU_MASK,
						     DCM_4444_ALP_MASK);
	    }
	    return translucentCM;
	} else {
	    return ColorModel.getRGBdefault();
	}
    }

    /**
     * Returns the default Transform for this configuration.  This
     * Transform is typically the Identity transform for most normal
     * screens.  Device coordinates for screen and printer devices will
     * have the origin in the upper left-hand corner of the target region of
     * the device, with X coordinates
     * increasing to the right and Y coordinates increasing downwards.
     * For image buffers, this Transform will be the Identity transform.
     */
    public AffineTransform getDefaultTransform() {
	return new AffineTransform();
    }

    /**
     *
     * Returns a Transform that can be composed with the default Transform
     * of a Graphics2D so that 72 units in user space will equal 1 inch
     * in device space.
     * Given a Graphics2D, g, one can reset the transformation to create
     * such a mapping by using the following pseudocode:
     * <pre>
     *      GraphicsConfiguration gc = g.getGraphicsConfiguration();
     *
     *      g.setTransform(gc.getDefaultTransform());
     *      g.transform(gc.getNormalizingTransform());
     * </pre>
     * Note that sometimes this Transform will be identity (e.g. for
     * printers or metafile output) and that this Transform is only
     * as accurate as the information supplied by the underlying system.
     * For image buffers, this Transform will be the Identity transform,
     * since there is no valid distance measurement.
     */
    public AffineTransform getNormalizingTransform() {
	double xscale = getXResolution() / 72.0;
	double yscale = getYResolution() / 72.0;
	return new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
    }

    private double getXResolution() {
        return 72.0;
    }
    private double getYResolution() {
        return 72.0;
    }

    public String toString() {
	return (super.toString()+"[dev="+screen+",pixfmt="+visual+"]");
    }

    private native Rectangle getBounds(int screen);
    
    public Rectangle getBounds() {
        return getBounds(screen.getScreen());
    }

    private static class DDrawBufferCapabilities extends BufferCapabilities {
        public DDrawBufferCapabilities(ImageCapabilities imageCaps) {
            super(imageCaps, imageCaps, FlipContents.PRIOR);
        }
        public boolean isFullScreenRequired() { return true; }
        public boolean isMultiBufferAvailable() { return true; }
    }
    
    private static class DDrawImageCapabilities extends ImageCapabilities {
        public DDrawImageCapabilities() {
            super(true);
        }
        public boolean isTrueVolatile() { return true; }
    }
    
    public BufferCapabilities getBufferCapabilities() {
        if (bufferCaps == null) {
            if (WindowsFlags.isDDEnabled()) {
                bufferCaps = new DDrawBufferCapabilities(
                    getImageCapabilities());
            } else {
                bufferCaps = super.getBufferCapabilities();
            }
        }
        return bufferCaps;
    }
    
    public ImageCapabilities getImageCapabilities() {
        if (imageCaps == null) {
            if (WindowsFlags.isDDEnabled()) {
                imageCaps = new DDrawImageCapabilities();
            } else {
                imageCaps = super.getImageCapabilities();
            }
        }
        return imageCaps;
    }

    public synchronized void displayChanged() {
	solidloops = null;
    }

    public void paletteChanged() {}

    /**
     * The following methods are invoked from WComponentPeer.java rather
     * than having the Win32-dependent implementations hardcoded in that
     * class.  This way the appropriate actions are taken based on the peer's
     * GraphicsConfig, whether it is a Win32GraphicsConfig or a
     * WGLGraphicsConfig.
     */

    /**
     * Creates a new SurfaceData that will be associated with the given
     * WComponentPeer.
     */
    public SurfaceData createSurfaceData(WComponentPeer peer,
                                         int numBackBuffers)
    {
        return Win32SurfaceData.createData(peer, numBackBuffers);
    }

    /**
     * Creates a new hidden-acceleration image of the given width and height
     * that is associated with the target Component.
     */
    public Image createAcceleratedImage(Component target,
                                        int width, int height)
    {
        ColorModel model = getColorModel(Transparency.OPAQUE);
        WritableRaster wr = 
            model.createCompatibleWritableRaster(width, height);
	return new OffScreenImage(target, model, wr,
                                  model.isAlphaPremultiplied());
    }

    /**
     * The following methods correspond to the multibuffering methods in
     * WComponentPeer.java...
     */

    private boolean isFullScreenExclusive(Component target) {
        Win32GraphicsDevice gd = (Win32GraphicsDevice)getDevice();
        while (target != null && !(target instanceof Window)) {
            target = target.getParent();
        }
        return (target == gd.getFullScreenWindow() &&
                gd.isDDEnabledOnDevice());
    }
    
    /**
     * Checks that the requested configuration is natively supported; if not,
     * an AWTException is thrown.
     */
    public void assertOperationSupported(Component target,
                                         int numBuffers,
                                         BufferCapabilities caps)
        throws AWTException
    {
        if (!isFullScreenExclusive(target)) {
            throw new AWTException(
                "The operation requested is only supported on a full-screen" +
                " exclusive window");
        }
    }

    /**
     * Creates a backbuffer for the given peer and returns the image wrapper.
     */
    public VolatileImage createBackBuffer(WComponentPeer peer) {
        // Create the back buffer object
        return new WinBackBuffer((Component)peer.getTarget(),
                                 (Win32SurfaceData)peer.getSurfaceData());
    }

    /**
     * Performs the native flip operation for the given target Component.
     */
    public void flip(WComponentPeer peer,
                     Component target, VolatileImage backBuffer,
                     BufferCapabilities.FlipContents flipAction)
    {
        int width = target.getWidth();
        int height = target.getHeight();
        if (flipAction == BufferCapabilities.FlipContents.COPIED) {
            Graphics g = target.getGraphics();
            g.drawImage(backBuffer, 0, 0, width, height, target);
            g.dispose();
        } else {
            try {
                Win32SurfaceData sd = (Win32SurfaceData)peer.getSurfaceData();
                WinBackBuffer winBackBuffer = (WinBackBuffer)backBuffer;
                sd.flip(winBackBuffer.getHWSurfaceData());
            } catch (sun.java2d.InvalidPipeException e) {
                return; // Flip failed
            }
            if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
                Graphics g = backBuffer.getGraphics();
                g.setColor(target.getBackground());
                g.fillRect(0, 0, width, height);
                g.dispose();
            }
        }
    }
}
