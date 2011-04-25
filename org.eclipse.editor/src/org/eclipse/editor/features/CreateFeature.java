package org.eclipse.editor.features;

import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateFeature extends AbstractCreateFeature {
    private static final String TITLE = "Create class";
    private static final String USER_QUESTION = "Enter new class name";

    public CreateFeature(IFeatureProvider fp) {
        super(fp, "EClass", "Create EClass");
    }

     public boolean canCreate(ICreateContext context) {
        return context.getTargetContainer() instanceof Diagram;
    }

    public Object[] create(ICreateContext context) {
        String newClassName = ExampleUtil.askString(TITLE, USER_QUESTION, "");
        if (newClassName == null || newClassName.trim().length() == 0) {
            return EMPTY;
        }

        // create EClass
        EClass newClass = EcoreFactory.eINSTANCE.createEClass();
        

        // Add model element to resource.
        // We add the model element to the resource of the diagram for
        // simplicity's sake. Normally, a customer would use its own
        // model persistence layer for storing the business model separately.
        getDiagram().eResource().getContents().add(newClass);
        newClass.setName(newClassName);

        // do the add
        addGraphicalRepresentation(context, newClass);

        // return newly created business object(s)
        return new Object[] { newClass };
    }
}
