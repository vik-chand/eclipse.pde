/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.text.IDocumentAttribute;
import org.eclipse.pde.internal.core.text.IDocumentRange;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.PDEMasterDetailsBlock;
import org.eclipse.pde.internal.ui.editor.PDESection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ExtensionPointsPage extends PDEFormPage {
	
	public static final String PAGE_ID = "ex-points"; //$NON-NLS-1$
	
	private ExtensionPointsSection fExtensionPointsSection;
	private ExtensionPointsBlock fBlock;
	
	public class ExtensionPointsBlock extends PDEMasterDetailsBlock {
		
		public ExtensionPointsBlock() {
			super(ExtensionPointsPage.this);
		}
		
		protected PDESection createMasterSection(IManagedForm managedForm,
				Composite parent) {
			fExtensionPointsSection = new ExtensionPointsSection(getPage(), parent);
			return fExtensionPointsSection;
		}
		
		protected void registerPages(DetailsPart detailsPart) {
			detailsPart.setPageProvider(new IDetailsPageProvider() {
				public Object getPageKey(Object object) {
					if (object instanceof IPluginExtensionPoint)
						return IPluginExtensionPoint.class;
					return object.getClass();
				}
				public IDetailsPage getPage(Object key) {
					if (key.equals(IPluginExtensionPoint.class))
						return new ExtensionPointDetails();
					return null;
				}
			});
		}
	}
	
	public ExtensionPointsPage(FormEditor editor) {
		super(editor, PAGE_ID, PDEUIMessages.ExtensionPointsPage_tabName);  
		fBlock = new ExtensionPointsBlock();
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText(PDEUIMessages.ExtensionPointsPage_title); 
		fBlock.createContent(managedForm);
		fExtensionPointsSection.fireSelection();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.MANIFEST_PLUGIN_EXT_POINTS);
	}
	
	public void updateFormSelection() {
		IFormPage page = getPDEEditor().findPage(PluginInputContext.CONTEXT_ID);
		if (page instanceof ManifestSourcePage) {
			ISourceViewer viewer = ((ManifestSourcePage)page).getViewer();
			if (viewer == null)
				return;
			StyledText text = viewer.getTextWidget();
			if (text == null)
				return;
			int offset = text.getCaretOffset();
			if (offset < 0)
				return;
			
			IDocumentRange range = ((ManifestSourcePage)page).getRangeElement(offset, true);
			if (range instanceof IDocumentAttribute)
				range = ((IDocumentAttribute)range).getEnclosingElement();
			if (range instanceof IPluginExtensionPoint)
				fExtensionPointsSection.selectExtensionPoint(new StructuredSelection(range));
		}
	}
}
