package com.banyaibalazs.createmoxplugin;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

class Mock {
    final PsiType type;
    final String name;

    public PsiType type() {
        return type;
    }

    public String name() {
        return name;
    }

    Mock(PsiType type, String name) {
        this.type = type;
        this.name = name;
    }

    public static Mock fromParameter(PsiParameter psiParameter) {
        PsiClassType fieldType = (PsiClassType) psiParameter.getTypeElement().getType();
        String fieldName = generateNameForMock(psiParameter);
        return new Mock(fieldType, fieldName);
    }

    public PsiField asField(final Project project) {
        final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        try {
            PsiField field = factory.createField(name, type);
            field.getModifierList().addAnnotation("org.mockito.Mock");
            return field;
        } catch (final IncorrectOperationException exc) {
            ApplicationManager.getApplication().invokeLater(
                    () -> Messages.showErrorDialog(
                            project,
                            "error.cannot.create.field.reason:" + exc.getClass().getSimpleName(),
                            CommonBundle.getErrorTitle()
                    )
            );
        }
        return null;
    }

    @NotNull
    private static String generateNameForMock(PsiParameter psiParameter) {
        return "mock" + capitalize(psiParameter.getName());
    }

    private static String capitalize(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

}
