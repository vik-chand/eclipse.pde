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
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginParent;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.text.IDocumentAttribute;
import org.eclipse.pde.internal.core.text.IDocumentRange;
import org.eclipse.pde.internal.core.text.IDocumentTextNode;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.PDEMasterDetailsBlock;
import org.eclipse.pde.internal.ui.editor.PDESection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ExtensionsPage extends PDEFormPage {
	public static final String PAGE_ID = "extensions"; //$NON-NLS-1$
	
	private ExtensionsSection fSection;
	private ExtensionsBlock fBlock;
	
	public class ExtensionsBlock extends PDEMasterDetailsBlock implements IDetailsPageProvider {
		public ExtensionsBlock() {
			super(ExtensionsPage.this);
		}
		protected PDESection createMasterSection(IManagedForm managedForm,
				Composite parent) {
			fSection = new ExtensionsSection(getPage(), parent);
			return fSection;
		}
		protected void registerPages(DetailsPart detailsPart) {
			detailsPart.setPageLimit(10);
			// register static page for the extensions
			detailsPart.registerPage(IPluginExtension.class, new ExtensionDetails());
			// register a dynamic provider for elements
			detailsPart.setPageProvider(this);
		}
		public Object getPageKey(Object object) {
			if (object instanceof IPluginExtension)
				return IPluginExtension.class;
			if (object instanceof IPluginElement) {
				ISchemaElement element = ExtensionsSection.getSchemaElement((IPluginElement)object);
				if (element!=null) return element;
				// no element - construct one
				IPluginElement pelement = (IPluginElement)object;
				String ename = pelement.getName();
				IPluginExtension extension = ExtensionsSection.getExtension((IPluginParent)pelement.getParent());
				return extension.getPoint()+"/"+ename; //$NON-NLS-1$
			}
			return object.getClass();
		}
		public IDetailsPage getPage(Object object) {
			if (object instanceof ISchemaElement)
				return new ExtensionElementDetails((ISchemaElement)object);
			if (object instanceof String)
				return new ExtensionElementDetails(null);
			return null;
		}
	}
	
	/**
	 * @param editor
	 * @param id
	 * @param title
	 */
	public ExtensionsPage(FormEditor editor) {
		super(editor, PAGE_ID, PDEUIMessages.ExtensionsPage_tabName);  
		fBlock = new ExtensionsBlock();
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		form.setText(PDEUIMessages.ExtensionsPage_title);
		fBlock.createContent(managedForm);
		BodyTextSection bodyTextSection = new BodyTextSection(this, form.getBody());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_BEGINNING);
		bodyTextSection.getSection().setLayoutData(gd);
		bodyTextSection.getSection().marginWidth = 5;
		managedForm.addPart(bodyTextSection);
		//refire selection
		fSection.fireSelection();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.MANIFEST_PLUGIN_EXTENSIONS);
		super.createFormContent(managedForm);
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
			else if (range instanceof IDocumentTextNode)
				range = ((IDocumentTextNode)range).getEnclosingElement();
			if (range != null)
				fSection.selectExtensionElement(new StructuredSelection(range));
		}
	}
}
