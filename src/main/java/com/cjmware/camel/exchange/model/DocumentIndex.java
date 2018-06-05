/*
 * Copyright (c) 2018.
 * THE SOURCE CODE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cjmware.camel.exchange.model;

import lombok.Data;

@Data
public class DocumentIndex {
    private String identifier; // A unique identifier associated with this conversion
    private Long processingDate; // The date the file was converted)
    private String fileName; // The name of the PDF file
    private Long   fileSize; // The file size of the PDF
    private String fileSystemPath; // The path to the PDF file in the file system
    private String originalFileType; // The original file type (doc, docx, odt, etc)
    private String originalFileName; // The original file name (not path)
    private Long   originalFileSize; // The original file size
}

