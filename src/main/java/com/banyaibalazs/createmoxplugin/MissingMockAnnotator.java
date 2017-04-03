package com.banyaibalazs.createmoxplugin;

import com.intellij.lang.annotation.*;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class MissingMockAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {

        if (element instanceof PsiConstructorCall) {
            PsiConstructorCall psiConstructorCall = ((PsiConstructorCall) element);
            int countOfActualArgs = psiConstructorCall.getArgumentList().getExpressions().length;
            PsiParameterList parameterList = psiConstructorCall.resolveConstructor().getParameterList();
            if (countOfActualArgs != parameterList.getParametersCount()) {
                PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
                holder
                        .createErrorAnnotation(element.getTextRange(), "Create mocks for this element")
                        .registerFix(new CreateMocksQuickFix(clazz, psiConstructorCall));
            }
        }
    }
}
