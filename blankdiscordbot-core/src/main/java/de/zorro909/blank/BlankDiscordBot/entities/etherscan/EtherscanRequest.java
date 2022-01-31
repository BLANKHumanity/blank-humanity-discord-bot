package de.zorro909.blank.BlankDiscordBot.entities.etherscan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Setter(value = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)
public class EtherscanRequest {

    public EtherscanRequest() {
	argument("module", () -> this.module().getValue());
	argument("action", () -> this.action().getValue());
	argument("apikey", this::apikey);
    }

    private List<ApiArgument> arguments = new ArrayList<>();

    @Setter
    @Getter
    @NotNull
    private EtherscanApiModule module;

    @Setter
    @Getter
    @NotNull
    private EtherscanApiAction action;

    @Setter
    @Getter
    @NotNull
    private String apikey;

    protected <T> void argument(String key, Supplier<T> getter) {
	argument(key, getter, false);
    }

    protected <T> void argument(String key, Supplier<T> getter,
	    boolean required) {
	arguments.add(new ApiArgument(key, getter, required));
    }

    public String toUrl() {
	return "https://api.etherscan.io/api?" + generateArguments();
    }

    public String generateArguments() {
	return arguments
		.stream()
		.map(this::applyArgument)
		.filter(Objects::nonNull)
		.collect(Collectors.joining("&"));
    }

    private String applyArgument(ApiArgument argument) {
	Optional<String> value = argument.getValue();
	if (value.isPresent()) {
	    return argument.getKey() + "=" + value.get();
	}
	if (argument.isRequired()) {
	    throw new RuntimeException("Argument '" + argument.getKey()
		    + "' is required for this request! (Request: "
		    + this.getClass().getName() + ")");
	}
	return null;
    }

    private record ApiArgument(String key, Supplier<?> getter,
	    boolean required) {

	public String getKey() {
	    return key;
	}

	public Optional<String> getValue() {
	    return Optional.ofNullable(getter.get()).map(Object::toString);
	}

	public boolean isRequired() {
	    return required;
	}

    }

}
