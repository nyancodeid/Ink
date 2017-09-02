/**
 * This file is part of Omegle API - Java.
 * <p>
 * Omegle API - Java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Omegle API - Java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with  Omegle API - Java.  If not, see <http://www.gnu.org/licenses/>.
 */
package kashmirr.omegle.event;

import org.json.JSONArray;

import java.util.Map;

import kashmirr.omegle.core.OmegleEvent;
import kashmirr.omegle.core.OmegleSession;
import kashmirr.omegle.core.OmegleSpyStranger;


public class OmegleEventAdaptor implements OmegleEventListener {

    @Override
    public void eventFired(OmegleSession session, OmegleEvent event,
                           JSONArray array) {
        // Nothing.
    }

    @Override
    public void chatWaiting(OmegleSession session) {
        // Nothing.
    }

    @Override
    public void chatConnected(OmegleSession session) {
        // Nothing.
    }

    @Override
    public void chatMessage(OmegleSession session, String message) {
        // Nothing.
    }

    @Override
    public void strangerDisconnected(OmegleSession session) {
        // Nothing.
    }

    @Override
    public void strangerTyping(OmegleSession session) {
        // Nothing.
    }

    @Override
    public void strangerStoppedTyping(OmegleSession session) {
        // Nothing.
    }

    @Override
    public void recaptchaRequired(OmegleSession session,
                                  Map<String, Object> variables) {
        // Nothing.
    }

    @Override
    public void recaptchaRejected(OmegleSession session,
                                  Map<String, Object> variables) {
        // Nothing.
    }

    @Override
    public void count(OmegleSession session, int count) {
        // Nothing.
    }

    @Override
    public void spyMessage(OmegleSession session, OmegleSpyStranger stranger,
                           String message) {
        // Nothing.
    }

    @Override
    public void spyTyping(OmegleSession session, OmegleSpyStranger stranger) {
        // Nothing.
    }

    @Override
    public void spyStoppedTyping(OmegleSession session,
                                 OmegleSpyStranger stranger) {
        // Nothing.
    }

    @Override
    public void spyDisconnected(OmegleSession session,
                                OmegleSpyStranger stranger) {
        // Nothing.
    }

    @Override
    public void question(OmegleSession session, String question) {
        // Nothing.
    }

    @Override
    public void omegleError(OmegleSession session, String string) {
        // Nothing.
    }

    @Override
    public void messageSent(OmegleSession session, String string) {
        // Nothing.
    }

    @Override
    public void chatDisconnected(OmegleSession session) {
        // Nothing.
    }

}
