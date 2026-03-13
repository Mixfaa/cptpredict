import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { html, LitElement, } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { TooltipController } from '@vaadin/component-base/src/tooltip-controller.js';
import { loader } from '@vaadin/vaadin-lumo-styles/mixins/loader.js';

@customElement('vcf-spinner')
export class VcfSpinner extends ThemableMixin(LitElement) {

  @property()
  theme : string | null = null;

  @property()
  label : string | null = 'Loading';

  static get is() {
    return 'vcf-spinner';
  }

  static get styles() {
	return loader.styleSheet;
  }

  _tooltipController : TooltipController | undefined;

  firstUpdated() {
	this._tooltipController = new TooltipController(this, 'tooltip');
    this.addController(this._tooltipController);
  }

  _set_theme(theme : string) {
    this.theme = theme;
  }

  render() {
	return html`
		<div
	      role='alert'
          aria-live='assertive'
          aria-label=${this.label}
          id='loader'
          part='loader'>
		</div>
		<slot name='tooltip'></slot>
	`;
  }
}