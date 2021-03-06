package org.eclipse.editor.features;

import static org.eclipse.editor.EditorUtil.nvl;

import org.eclipse.editor.AbstractPropertySection;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class TextboxPropertySection<T> extends AbstractPropertySection<T, String> {
	private Text text;

	@Override
	protected void createEditElement(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		text = factory.createText(composite, "");

		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		text.setLayoutData(data);

		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent _) {
				if (!nvl(getValue()).equals(text.getText())) {
					doInTransaction(new Runnable() {
						@Override
						public void run() {
							setValue(text.getText());
						}
					});
				}
			}
		});
	}

	@Override
	public void refresh() {
		text.setText(nvl(getValue()));
	}
}
