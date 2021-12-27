/*
 * MATLAB Compiler: 8.1 (R2020b)
 * Date: Fri Dec  3 11:16:21 2021
 * Arguments: 
 * "-B""macro_default""-W""java:Online_Novelty,NoveltyClass""-T""link:lib""-d""C:\\Users\\Jose 
 * Bailon\\Desktop\\FEMIoT\\P2\\UC2_CODE\\Matlab\\Online_Novelty\\for_testing""class{NoveltyClass:C:\\Users\\Jose 
 * Bailon\\Desktop\\FEMIoT\\P2\\UC2_CODE\\Matlab\\Online_Novelty.m}"
 */

package Online_Novelty;

import com.mathworks.toolbox.javabuilder.pooling.Poolable;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>NoveltyClassRemote</code> class provides a Java RMI-compliant interface to 
 * MATLAB functions. The interface is compiled from the following files:
 * <pre>
 *  C:\\Users\\Jose Bailon\\Desktop\\FEMIoT\\P2\\UC2_CODE\\Matlab\\Online_Novelty.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>NoveltyClassRemote</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed, and the server-side proxy is unexported.  (Failure to call 
 * dispose may result in server-side threads not being properly shut down, which often 
 * appears as a hang.)  
 *
 * This interface is designed to be used together with 
 * <code>com.mathworks.toolbox.javabuilder.remoting.RemoteProxy</code> to automatically 
 * generate RMI server proxy objects for instances of Online_Novelty.NoveltyClass.
 */
public interface NoveltyClassRemote extends Poolable
{
    /**
     * Provides the standard interface for calling the <code>Online_Novelty</code> MATLAB 
     * function with 6 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * Documentation as provided by the author of the MATLAB function:
     * <pre>
     * {@literal 
	 * %% Load data
	 * }
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the MATLAB function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.rmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] Online_Novelty(int nargout, Object... rhs) throws RemoteException;
  
    /** 
     * Frees native resources associated with the remote server object 
     * @throws java.rmi.RemoteException An error has occurred during the function call or in communication with the server.
     */
    void dispose() throws RemoteException;
}
