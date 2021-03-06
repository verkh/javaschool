package com.ecare.controllers;

import com.ecare.dto.*;
import com.ecare.services.AuthService;
import com.ecare.services.ContractService;
import com.ecare.services.OptionsService;
import com.ecare.services.PlanService;
import com.ecare.validators.PlanValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles buisness logic of plans management which includes also user's choice of new plan
 */
@Controller
@SessionAttributes(names = "Plan")
public class PlansController {

    private static Logger logger = LogManager.getLogger(PlansController.class);

    @Autowired
    PlanService planService;

    @Autowired
    ContractService contractService;

    @Autowired
    protected AuthService authService;

    @Autowired
    OptionsService optionsService;

    @Autowired
    PlanValidator planValidator;

    /**
     * Configures plans page for normal users
     * @param model model for JSP page
     * @return the name of JSP file
     */
    @RequestMapping(value = "/plans")
    public String getPlans(ModelMap model) {
        logger.trace("Configuring plans page...");
        List<Plan> plans = planService.getAll();
        model.addAttribute("Plans", plans);
        return "Plans";
    }

    /**
     * Configures plan page
     * @param model model for JSP page
     * @param id id of needed plan
     * @return the name of JSP file
     */
    @RequestMapping(value = "/plans/{id}")
    public String getPlan(ModelMap model, @PathVariable long id) {
        logger.trace("Configuring plan page with id=" + id);
        Plan plan = planService.get(id).get();
        System.out.println(plan.getId() + " " + plan.getName() + " " + plan.getOptions().size());
        model.addAttribute("Plan", plan);
        return "Plan";
    }

    /**
     * Configures plans page with pagination for administrators
     * @param model model for JSP page
     * @param currentPage
     * @param deleteId
     * @return the name of JSP file
     */
    @RequestMapping(value = "/administration/tariffs")
    public String getPlans(ModelMap model,
                           @RequestParam(value = "currentPage", required = false) Integer currentPage,
                           @RequestParam(value = "delete", required = false) Long deleteId
    ) {
        logger.trace("Configuring plans page for administrators...");
        if(deleteId != null)
            planService.delete(new Plan(deleteId).toBuilder().build());
        Utils.pagination(planService, model, currentPage, "tariffs");
        return "administration/Tariffs";
    }

    /**
     * Configures plan page for administrator
     * @param model model for JSP page
     * @param id id of the needed plan
     * @return the name of JSP file
     */
    @RequestMapping(value = "/administration/tariffs/{id}", method = RequestMethod.GET)
    public String getTariff(ModelMap model, @PathVariable long id) {
        logger.trace("Configuring plan page for administrators with id=" + id);
        Plan plan = planService.get(id).get();

        List<Option> options = Utils.prepareOptions(plan.getOptions(), optionsService.getAll());
        plan.setOptions(options);
        model.addAttribute("current_action", id);
        model.addAttribute("Plan", plan);
        return "administration/Tariff";
    }

    /**
     * Configures page for a new plan for administrator
     * @param model model for JSP page
     * @return the name of JSP file
     */
    @RequestMapping(value = "/administration/tariffs/new", method = RequestMethod.GET)
    public String getTariff(ModelMap model) {
        logger.trace("Configuring new plan page for administrators...");
        Plan plan = Plan.builder().build();

        List<Option> options = Utils.prepareOptions(plan.getOptions(), optionsService.getAll());
        plan.setOptions(options);
        model.addAttribute("current_action", "new");
        model.addAttribute("Plan", plan);
        return "administration/Tariff";
    }


    /**
     * Attempts to save new plan
     * @param model model for JSP page
     * @param plan filled new plan
     * @param result validation errors container
     * @return the name of JSP file
     */
    @RequestMapping(value = {"/administration/tariffs/{id}", "/administration/tariffs/new"}, method = RequestMethod.POST)
    public String saveTariff(ModelMap model,
                             @ModelAttribute(value="Plan") Plan plan, BindingResult result
    ){
        logger.trace(String.format("Saving plan %s with id=%d", plan.getName(), plan.getId()));
        planValidator.validate(plan, result); // validation should be done before filtering of disabled options
        if(result.hasErrors()) {
            logger.trace(String.format("Failed to save plan %s with id=%d due multiple errors", plan.getName(), plan.getId()));
            return "administration/Tariff";
        }

        Plan planForSave = plan.toBuilder().build();
        planForSave.setOptions(new ArrayList<>());

        for(final Option opt : plan.getOptions()) {
            if(opt.isEnabled())
                planForSave.getOptions().add(opt);
        }

        if(plan.getId() != null)
            planService.update(planForSave);
        else
            planService.save(planForSave);
        logger.trace(String.format("Saved plan %s with id=%d", plan.getName(), plan.getId()));
        model.addAttribute("Plan", plan);
        return "redirect:/administration/tariffs";
    }

    /**
     * Applies plan for authenticated user
     * @param model model for JSP page
     * @param id id of new plan
     * @return the name of JSP file
     */
    @RequestMapping(value = "/plans/plan")
    public String applyPlan(ModelMap model,
                            HttpServletRequest request,
                            @RequestParam(value = "apply", required = false) Long id,
                            @ModelAttribute(value = "userCart") Cart cart
    ) {
        Contract contract = authService.getCurrentUser();
        if(cart == null || contract == null) {
            return "redirect:/auth";
        } else if(!cart.isInited()) {
            cart.init(contract);
        }

        Plan plan = planService.get(id).get();
        cart.getNewContract().setPlan(plan);
        cart.getNewContract().setOptions(plan.getOptions());

        for(final Option newOpt : cart.getNewContract().getOptions()) {
            if(newOpt.isUndisablable()) {
                newOpt.setEnabled(true);
                continue;
            }

            final Option oldOption = cart.getOriginalContract().getOptions().stream()
                    .filter(old -> newOpt.getId().equals(old.getId())).findAny().orElse(null);
            if(oldOption != null)
                newOpt.setEnabled(oldOption.isEnabled());
        }

        request.getSession().setAttribute("userCart", cart);

        logger.trace(String.format("Apply plan %s with id=%d", plan.getName(), plan.getId()));
//        contractService.save(contract);

        return "redirect:/contract";
    }
}