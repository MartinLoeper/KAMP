package edu.kit.ipd.sdq.kamp.ruledsl.support;

import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.buildProject;
import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.getBundleNameForProjectName;
import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.getDslBundle;
import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.getProject;
import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.installAndStartProjectBundle;
import static edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil.registerProjectBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

// this is intentionally a Java class NOT xtend because of import problems for plugins calling xtend files
public final class KampRuleLanguageFacade {
	private static final BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageFacade.class).getBundleContext();
	
	private KampRuleLanguageFacade() {}
	
	public static class KampLanguageService<T> implements AutoCloseable {
		private final ServiceReference<T> serviceReference;
		private final String projectName;
		private final T service;
		private Class<T> serviceClass;

		public KampLanguageService(String projectName, Class<T> serviceClass) throws BundleException, CoreException {
			this.projectName = projectName;
			this.serviceClass = serviceClass;
			this.serviceReference = getServiceReference(projectName, serviceClass);
			
			if(this.serviceReference == null) {
				throw new IllegalStateException("ServiceReference not found! The bundle for " + projectName + " is probably not registered.");
			}
			
			this.service = bundleContext.getService(serviceReference);
			
			if(this.service == null) {
				throw new IllegalStateException("Service not found! The bundle for " + projectName + " is probably not registered.");
			} else {
				System.out.println("Dsl Service for project obtained: " + projectName);
			}
		}
		
		public T getService() {
			return this.service;
		}

		@Override
		public void close() throws Exception {
			if(this.serviceReference != null)
				unregisterService(serviceReference);
			
			System.out.println("Leaving DSL bundle service lookup.");
		}
		
	}
	
	public static <T> void unregisterService(ServiceReference<T> serviceReference) {	
		BundleContext bundleContext = FrameworkUtil.getBundle(KampRuleLanguageFacade.class).getBundleContext();
	 	bundleContext.ungetService(serviceReference);
	}
	
	public static <T> ServiceReference<T> getServiceReference(String sourceProjectName, Class<T> serviceReferenceClazz) throws BundleException, CoreException {
    	Bundle dslBundle = installIfNecessaryAndGetBundle(sourceProjectName, new NullProgressMonitor());
	    
	    if(dslBundle != null) {
	 		 System.out.println("Found bundle with additional propagation rules. State: " + dslBundle.getState());
	 		 if(dslBundle.getState() == Bundle.ACTIVE) {
	 			@SuppressWarnings("unchecked")
				ServiceReference<T> serviceReference = (ServiceReference<T>) bundleContext.getServiceReference(serviceReferenceClazz.getName());
	 			
	 			return serviceReference;
	 		 } else {
	 			 System.err.println("Bundle not activated??! Error.");
	 			 // TODO handle error
	 		 }
	    } else {
	    	System.err.println("Could not start custom DSL bundle.");
	    }
	    
	    return null;
	}

	public static Bundle installIfNecessaryAndGetBundle(String sourceProjectName, IProgressMonitor mon) throws BundleException, CoreException {
		SubMonitor subMonitor = SubMonitor.convert(mon, "Installing Dsl Bundle", 6);
		
		subMonitor.split(1).beginTask("Search for bundle", 3);
		Bundle dslBundle = null;
	    /* lookup the kamp dsl bundle */
	    for(Bundle bundle : bundleContext.getBundles()) {
	    	if(bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(getBundleNameForProjectName(sourceProjectName))) {
	   	  		dslBundle = bundle;
	   	  	}
	    }
	   
	    /* if the bundle is not already loaded, try to load in manually */
	    
	    if(dslBundle == null) {
	    	subMonitor.split(1).beginTask("Install bundle at OSGi layer", 1);
	    	System.out.println("Registering bundle manually...");
	    	Bundle startedBundle = buildProjectAndInstall(sourceProjectName, subMonitor.split(3));
	    	// wait for bundle to start
	    	// TODO is busy waiting ok in KAMP here?
	    	subMonitor.split(1).beginTask("Wait for bundle state ACTIVE", 1);
	    	while(startedBundle != null && startedBundle.getState() != Bundle.ACTIVE) {};
	    	
	    	dslBundle = startedBundle;
	    }
	    subMonitor.done();
	    
	    return dslBundle;
	}

	// basically a convenience method
	public static Bundle buildProjectAndInstall(String sourceProjectName, IProgressMonitor monitor) throws BundleException, CoreException {
		buildProject(getProject(sourceProjectName), null);
    	
		return installAndStartProjectBundle(sourceProjectName, monitor);
	}
	
	// basically a convenience method
	public static Bundle buildProjectAndReInstall(String sourceProjectName, IProgressMonitor monitor) throws CoreException, BundleException {
		buildProject(getProject(sourceProjectName), null);
    	
		return registerProjectBundle(getProject(sourceProjectName), getDslBundle(sourceProjectName), monitor);
	}

	public static <T> KampLanguageService<T> getInstance(String projectName, Class<T> serviceClass) throws BundleException, CoreException {
		return new KampLanguageService<T>(projectName, serviceClass);
	}
	
	// a modified folder at root indicates that the given project is a kamp project
	public static boolean isKampProjectFolder(IProject project) {
		return project.getFolder("modified").exists();
	}

	public static boolean isKampDslRuleProjectFolder(IProject project) {
		return project.getName().endsWith("-rules");
	}
}
