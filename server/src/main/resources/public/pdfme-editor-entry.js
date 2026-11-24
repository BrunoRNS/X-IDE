import { BLANK_A4_PDF } from '@pdfme/common';
import { generate } from '@pdfme/generator';
import { image, signature, text, barcodes, line, rectangle, ellipse, table } from '@pdfme/schemas';
import { Designer } from '@pdfme/ui';

// PDF editor plugin configuration
const plugins = {
    text,
    image,
    signature,
    qrcode: barcodes.qrcode,
    line,
    rectangle,
    ellipse,
    table,
};

window.ProblemPdfEditor = {
    BLANK_A4_PDF,
    Designer,
    generate,
    plugins,
};
