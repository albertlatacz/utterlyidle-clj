package utterlyidle;

import clojure.lang.IFn;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.cookies.CookieParameters;
import com.googlecode.utterlyidle.dsl.DefinedParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static clojure.lang.Reflector.invokeInstanceMember;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.UriTemplate.uriTemplate;

public class ClojureBinding {

    public static Binding binding(String path, String method, String[] consumes, String[] produces, IFn function, Pair<Type, Option<Parameter>>[] params) throws NoSuchMethodException {
        return new Binding(
                dispatchMethod(),
                uriTemplate(path),
                method,
                sequence(consumes),
                sequence(produces),
                dispatchMethodParameters(function, params),
                1, false);
    }

    private static Sequence<Pair<Type, Option<Parameter>>> dispatchMethodParameters(IFn function, Pair<Type, Option<Parameter>>[] params) {
        return sequence(functionParam(function))
                .join(sequence(params));
    }

    public static Pair<Type, Option<Parameter>> queryParam(String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, QueryParameters.class, option((String) null))));
    }

    public static Pair<Type, Option<Parameter>> formParam(String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, FormParameters.class, option((String) null))));
    }

    public static Pair<Type, Option<Parameter>> cookieParam(String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, CookieParameters.class, option((String) null))));
    }

    public static Pair<Type, Option<Parameter>> headerParam(String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, HeaderParameters.class, option((String) null))));
    }

    public static Pair<Type, Option<Parameter>> pathParam(String name) {
        return Pair.pair((Type) String.class, Option.<Parameter>some(new NamedParameter(name, PathParameters.class, option((String) null))));
    }

    private static Pair<Type, Option<Parameter>> functionParam(IFn value) {
        return Pair.pair((Type) IFn.class, Option.<Parameter>some(new DefinedParameter<IFn>(IFn.class, value)));
    }

    public static Pair<Type, Option<Parameter>> requestParam() {
        return Pair.pair((Type) Request.class, Option.<Parameter>none());
    }

    private static Method dispatchMethod() {
        return method(on(ClojureBinding.class).invoke()).method();
    }

    public Object invoke(Object... params) {
        return invokeInstanceMember("invoke", params[0], sequence(params).tail().toArray(Object.class));
    }
}
