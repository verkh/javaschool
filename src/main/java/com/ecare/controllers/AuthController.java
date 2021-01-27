package com.ecare.controllers;

import com.ecare.dao.Dao;
import com.ecare.models.ContractPO;
import com.ecare.models.PlanPO;
import com.ecare.models.UserPO;
import com.ecare.services.PlanService;
import com.ecare.validators.NewContractValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * AuthContoller handles authentication buisness loginc such as authentication and registration
 */
@Controller
@SessionAttributes("availablePlans")
public class AuthController extends BaseUserController {

    private static Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private NewContractValidator newContractValidator;

    @Autowired
    private PlanService planService;

    /**
     * Configures sign in JSP page
     * @param model model for JSP
     * @param error the placeholder for validation error
     * @return the name of JSP page
     */
    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    public String getSignIn(ModelMap model,
                            @RequestParam(value = "error", required = false) String error)
    {
        logger.trace("Configuring sign in page...");
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!" + error);
        }

        return "SignIn";
    }

    /**
     * Configures sign up JSP page for new user
     * @param model model for JSP
     * @param error placeholder for error if occured
     * @return the name of JSP file
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getSignUp(ModelMap model,
                            @RequestParam(value = "error", required = false) String error)
    {
        UserPO curLoggedUser = authService.getCurrentUser();
        if(curLoggedUser != null && !curLoggedUser.isAdmin())
            return "redirect:/profile";

        logger.trace("Configuring register page...");
        model.addAttribute("contract", new ContractPO());
        model.addAttribute("availablePlans", planService.getAll());
        prepare(model, Type.Registration);
        return "Profile";
    }

    /**
     * Tries to register new user, validates an input
     * @param model model for JSP page
     * @param newContract filled new contract
     * @param result the errors container
     * @return the name of JSP file
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String submitSignUp(ModelMap model,
                               @ModelAttribute(value="contract") ContractPO newContract,
                               BindingResult result
    ) {
        logger.trace(String.format("Submit register is received for new user with name: %s %s",
                newContract.getUser().getName(),
                newContract.getUser().getLastName()));

        prepare(model, Type.Registration);
        UserPO newUser = newContract.getUser();
        newUser.setPasswordHash(passwordEncoder.encode(newUser.getRawPassword()));
        newContractValidator.validate(newContract, result);

        if(result.hasErrors()) {
            result.getFieldErrors().stream().
                    forEach(f -> logger.debug(String.format("Next error were detected: %s - %s",
                            f.getField(), f.getDefaultMessage())));
            return "Profile";
        }

        PlanPO plan = planService.get(newContract.getPlan().getId()).get();
        newContract.setPlan(plan);
        newContract.setOptions(plan.getOptions());

        contractService.save(newContract);
        setSuccess(model, "You've been successfully registered!");

        logger.trace(String.format("New user '%s' is successfully registered", newUser.getEmail()));

        UserPO currentUser = authService.getCurrentUser();
        if(currentUser != null && currentUser.isAdmin())
            return "redirect:/administration/contracts/" + newContract.getId();

        return "redirect:/auth";
    }
}
