package ro.iss.domain.validator;

import ro.iss.domain.Reader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaderValidator implements Validator<Reader> {

    @Override
    public void validate(Reader reader) throws ValidationException {
        String errorMessage = "";

        String cnpRegex = "^[1-9][0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])(0[1-9]|[1-4][0-8])([0-9]{2}[1-9]|[0-9][1-9][0-9]|[1-9][0-9]{2})[0-9]$";
        String nrTelRegex = "^[0-9]{10}";
        String nameRegex = "^[A-Za-z\\-\\s]+$";

        Pattern cnpPattern = Pattern.compile(cnpRegex);
        Pattern nrTelPattern = Pattern.compile(nrTelRegex);
        Pattern namePattern = Pattern.compile(nameRegex);

        Matcher cnpMatcher = cnpPattern.matcher(reader.getCNP());
        Matcher nrTelMatcher = nrTelPattern.matcher(reader.getPhoneNumber());

        if (!cnpMatcher.matches()) {
            errorMessage += "CNP is incorrect\n";
        }

        if (!nrTelMatcher.matches()) {
            errorMessage += "Phone number is incorrect\n";
        }

        if (!namePattern.matcher(reader.getFirstName()).matches()) {
            errorMessage += "First name is invalid\n";
        }

        if (!namePattern.matcher(reader.getLastName()).matches()) {
            errorMessage += "Last name is invalid\n";
        }

        if (reader.getFirstName().length() < 3) {
            errorMessage += "First name must be at least 3 characters\n";
        }

        if (reader.getLastName().length() < 3) {
            errorMessage += "Last name must be at least 3 characters\n";
        }

        if (reader.getUsername().length() < 5) {
            errorMessage += "Username must be at least 5 characters\n";
        }

        if (reader.getPassword().length() < 6) {
            errorMessage += "Password must be at least 6 characters\n";
        }

        if (!errorMessage.isEmpty()) {
            throw new ValidationException(errorMessage);
        }

    }
}
