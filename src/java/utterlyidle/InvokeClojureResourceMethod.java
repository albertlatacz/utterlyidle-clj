package utterlyidle;

import clojure.lang.*;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.Binding;
import com.googlecode.utterlyidle.bindings.MatchedBinding;
import com.googlecode.utterlyidle.bindings.actions.Action;
import com.googlecode.utterlyidle.bindings.actions.ActionMetaData;
import com.googlecode.utterlyidle.cookies.CookieParameters;
import com.googlecode.utterlyidle.dsl.DefinedParameter;
import com.googlecode.yadic.Container;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static clojure.lang.Reflector.invokeInstanceMember;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.UriTemplate.uriTemplate;

public class InvokeClojureResourceMethod implements Action {
    public static Binding binding(String path, String method, String[] consumes, String[] produces, IFn function, Pair<Type, Option<Parameter>>[] params) throws NoSuchMethodException {
        return new Binding(
                dispatchAction(),
                uriTemplate(path),
                method,
                sequence(consumes),
                sequence(produces),
                dispatchMethodParameters(function, params),
                1, false, null);
    }

    private static Sequence<Pair<Type, Option<Parameter>>> dispatchMethodParameters(IFn function, Pair<Type, Option<Parameter>>[] params) {
        return sequence(functionParam(function))
                .join(sequence(params));
    }

    public static Pair<Type, Option<Parameter>> queryParam(String name) {
        return namedParameter(QueryParameters.class, name);
    }

    public static Pair<Type, Option<Parameter>> formParam(String name) {
        return namedParameter(FormParameters.class, name);
    }

    public static Pair<Type, Option<Parameter>> cookieParam(String name) {
        return namedParameter(CookieParameters.class, name);
    }

    public static Pair<Type, Option<Parameter>> headerParam(String name) {
        return namedParameter(HeaderParameters.class, name);
    }

    public static Pair<Type, Option<Parameter>> pathParam(String name) {
        return namedParameter(PathParameters.class, name);
    }

    public static Type customType(final String name) {
        return new ParameterizedType() {
            public int hashCode() {
                return name.hashCode();
            }

            public boolean equals(Object obj) {
                return name.equals(obj);
            }

            public String toString() {
                return "Custom Type [" + name + "]";
            }

            public Type[] getActualTypeArguments() {
                return new Type[0];
            }

            public Type getRawType() {
                return getClass();
            }

            public Type getOwnerType() {
                return getClass();
            }
        };
    }

    private static Pair<Type, Option<Parameter>> functionParam(IFn value) {
        return Pair.pair((Type) IFn.class, Option.<Parameter>some(new DefinedParameter<IFn>(IFn.class, value)));
    }

    public static Pair<Type, Option<Parameter>> requestParam() {
        return Pair.pair((Type) Request.class, Option.<Parameter>none());
    }

    private static Action dispatchAction() {
        return new InvokeClojureResourceMethod();
    }

    private static Pair<Type, Option<Parameter>> namedParameter(Class<? extends Parameters<String, String, ?>> parametersClass, String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, parametersClass, option((String) null))));
    }

    public Object invoke(Container requestScope) throws Exception {
        Request request = requestScope.get(Request.class);
        final Application application = requestScope.get(Application.class);
        Binding binding = requestScope.get(MatchedBinding.class).value();
        Object[] params = new ParametersExtractor(binding.uriTemplate(), application, binding.parameters()).extract(request);


        IPersistentMap param = ((IMeta) params[0]).meta();

        Object bindingKW = Keyword.intern("binding");
        IPersistentMap bindings = (IPersistentMap) param.valAt(bindingKW);
        Object scopedParamKW = Keyword.intern("scoped-params");

        Sequence<Object> applicationScoped = sequence((Iterable<Iterable<Object>>) bindings.valAt(scopedParamKW))
                .map(new Function1<Iterable<Object>, Object>() {
                    @Override
                    public Object call(Iterable<Object> o) throws Exception {
                        return application.applicationScope().resolve(customType(sequence(o).first().toString()));
                    }
                });

        try {
            return invokeInstanceMember("invoke", params[0], applicationScoped.join(sequence(params).tail()).toArray(Object.class));
        } catch (Exception e) {
            System.out.println("e = " + e);
            return null;
        }

    }

    public String description() {
        return toString();
    }

    public Iterable<ActionMetaData> metaData() {
        return Sequences.empty();
    }
}
