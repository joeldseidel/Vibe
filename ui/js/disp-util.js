/**
 * Display macro functions
 */

function displayFormControlError(control, error){
    control.val("");
    control.addClass('is-invalid');
    control.attr("placeholder", error);
}