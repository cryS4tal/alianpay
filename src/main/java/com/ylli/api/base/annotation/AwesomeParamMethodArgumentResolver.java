package com.ylli.api.base.annotation;

import com.google.common.base.CaseFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;
import org.springframework.web.util.WebUtils;

/**
 * Created by RexQian on 2017/2/14.
 */
public class AwesomeParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {
    private boolean isEnableLowerUnderscoreName = false;

    public void setLowerUnderscoreName(boolean isEnable) {
        this.isEnableLowerUnderscoreName = isEnable;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        AwesomeParam ann = parameter.getParameterAnnotation(AwesomeParam.class);
        return (ann != null ? new AwesomeParamNamedValueInfo(ann) : new AwesomeParamNamedValueInfo());
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AwesomeParam.class);
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        if (isEnableLowerUnderscoreName) {
            name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        }
        return internalResolveName(name, parameter, request);
    }

    /**
     * @see RequestParamMethodArgumentResolver
     */
    private Object internalResolveName(String name, MethodParameter parameter,
                                       NativeWebRequest request) throws Exception {

        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        MultipartHttpServletRequest multipartRequest =
                WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);

        Object mpArg = MultipartResolutionDelegate.resolveMultipartArgument(name, parameter, servletRequest);
        if (mpArg != MultipartResolutionDelegate.UNRESOLVABLE) {
            return mpArg;
        }

        Object arg = null;
        if (multipartRequest != null) {
            List<MultipartFile> files = multipartRequest.getFiles(name);
            if (!files.isEmpty()) {
                arg = (files.size() == 1 ? files.get(0) : files);
            }
        }
        if (arg == null) {
            String[] paramValues = request.getParameterValues(name);
            if (paramValues != null) {
                arg = (paramValues.length == 1 ? paramValues[0] : paramValues);
            }
        }
        return arg;
    }

    private static class AwesomeParamNamedValueInfo extends NamedValueInfo {

        public AwesomeParamNamedValueInfo() {
            super("", false, ValueConstants.DEFAULT_NONE);
        }

        public AwesomeParamNamedValueInfo(AwesomeParam annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }

}
