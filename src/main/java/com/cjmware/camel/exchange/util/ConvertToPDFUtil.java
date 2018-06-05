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

package com.cjmware.camel.exchange.util;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConvertToPDFUtil {

    public static String getISODateFolderName() {

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        return date.format(formatter);
    }

    public static String getConvertedFilePath(String originalFilePath, String outputFileFolderName, String newExtension) {
        File orifinalFile = new File(originalFilePath);
        String originalFileName = orifinalFile.getName();
        String convertedFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")) +"." +newExtension;
        String convertedFilePath = outputFileFolderName +File.separator +convertedFileName;
        return convertedFilePath;
    }
}
