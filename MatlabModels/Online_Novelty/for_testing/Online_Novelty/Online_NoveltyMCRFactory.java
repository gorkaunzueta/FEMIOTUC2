/*
 * MATLAB Compiler: 8.1 (R2020b)
 * Date: Fri Dec  3 11:16:21 2021
 * Arguments: 
 * "-B""macro_default""-W""java:Online_Novelty,NoveltyClass""-T""link:lib""-d""C:\\Users\\Jose 
 * Bailon\\Desktop\\FEMIoT\\P2\\UC2_CODE\\Matlab\\Online_Novelty\\for_testing""class{NoveltyClass:C:\\Users\\Jose 
 * Bailon\\Desktop\\FEMIoT\\P2\\UC2_CODE\\Matlab\\Online_Novelty.m}"
 */

package Online_Novelty;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class Online_NoveltyMCRFactory
{
    /** Component's uuid */
    private static final String sComponentId = "Online_Novel_a38f13a2-8b6c-4ca3-9ae4-a097a3bb5ee4";
    
    /** Component name */
    private static final String sComponentName = "Online_Novelty";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(Online_NoveltyMCRFactory.class)
        );
    
    
    private Online_NoveltyMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            Online_NoveltyMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,9,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
