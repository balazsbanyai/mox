package com.banyaibalazs.createmoxplugin;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.uiDesigner.lw.IContainer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

class CreateMocksQuickFix extends BaseIntentionAction {
    private PsiClass clazz;
    private PsiParameterList psiParameterList;
    private PsiConstructorCall call;

    CreateMocksQuickFix(PsiClass clazz, PsiParameterList psiParameterList, PsiConstructorCall call) {
        this.clazz = clazz;
        this.psiParameterList = psiParameterList;
        this.call = call;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create mocks";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Simple properties";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, PsiFile file) throws
            IncorrectOperationException {

        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().assertReadAccessAllowed();

            PsiDocumentManager.getInstance(project).commitAllDocuments();

            if(!clazz.isValid()){
                return;
            }

            CommandProcessor.getInstance().executeCommand(
                    project,
                    () -> ApplicationManager.getApplication().runWriteAction(
                            () -> createMocks(project, file)
                    ),
                    "create mox",
                    null
            );

        });
    }

    private void createMocks(Project project, PsiFile file) {

        List<String> addedArgs = new ArrayList<>();
        PsiExpressionList args = call.getArgumentList();
        for (PsiParameter psiParameter : psiParameterList.getParameters()) {
            PsiClassType clzz = (PsiClassType) psiParameter.getTypeElement().getType();
            PsiClass fieldType = clzz.rawType().resolve();



            String fieldName = "mock" + capitalize(psiParameter.getName());

            PsiField field = createField(project, fieldType, fieldName, clazz, true, null);

            if (!hasField(clazz, field)) {
                clazz.add(field);
                addedArgs.add(fieldName);
            }
        }

        String argList = String.join(", ", addedArgs);
        int beginningOfStatement = args.getTextOffset();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);
        document.insertString(beginningOfStatement+1, argList);

        //CodeStyleManager.getInstance(project).reformat(call);

    }

    private boolean hasField(PsiClass clazz, PsiField field) {
        return Stream.of(clazz.getFields()).anyMatch(psiField -> psiField.getName().equals(field.getName()));
    }

    public static String capitalize(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private PsiField createField(final Project project,
                                    final PsiClass fieldClass,
                                    final String fieldName,
                                    final PsiClass boundClass,
                                    final boolean showErrors,
                                    final IContainer rootContainer) {
        // 1. Create field
        final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        final PsiType type = factory.createType(fieldClass);
        try {
            final PsiField field = factory.createField(fieldName, type);

            final PsiModifierList modifierList = field.getModifierList();
            assert modifierList != null;
            String[] modifiers = {PsiModifier.PRIVATE, PsiModifier.PROTECTED, PsiModifier.PUBLIC};
//            for(@PsiModifier.ModifierConstant String modifier: modifiers) {
//                modifierList.setModifierProperty(modifier, accessibility.equals(modifier));
//            }
            PsiField lastUiField = null;
            for(PsiField uiField: boundClass.getFields()) {
//                if (FormEditingUtil.findComponentWithBinding(rootContainer, uiField.getName()) != null) {
//                    lastUiField = uiField;
//                }
            }
            if (lastUiField != null) {
                //boundClass.addAfter(field, lastUiField);
            }
            else {
                //boundClass.add(field);
            }

            return field;
        }
        catch (final IncorrectOperationException exc) {
            if (showErrors) {
                ApplicationManager.getApplication().invokeLater(
                        () -> Messages.showErrorDialog(
                                project,
                                "error.cannot.create.field.reason"+exc.getClass().getSimpleName(),
                                CommonBundle.getErrorTitle()
                        )
                );
            }
        }
        return null;
    }
}