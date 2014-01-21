package utterlyidle;

import clojure.lang.IFn;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.bindings.MatchedBinding;
import com.googlecode.utterlyidle.bindings.actions.Action;
import com.googlecode.utterlyidle.bindings.actions.ActionMetaData;
import com.googlecode.utterlyidle.cookies.CookieParameters;
import com.googlecode.utterlyidle.dsl.DefinedParameter;
import com.googlecode.yadic.Container;

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

    public Object invoke(Container container) throws Exception {
        Request request = container.get(Request.class);
        Application application = container.get(Application.class);
        Binding binding = container.get(MatchedBinding.class).value();
        Object[] params = new ParametersExtractor(binding.uriTemplate(), application, binding.parameters()).extract(request);
        return invokeInstanceMember("invoke", params[0], sequence(params).tail().toArray(Object.class));
    }

    public String description() {
        return toString();
    }

    public Iterable<ActionMetaData> metaData() {
        return Sequences.empty();
    }
}
