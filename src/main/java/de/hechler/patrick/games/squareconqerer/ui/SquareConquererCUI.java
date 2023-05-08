// This file is part of the Square Conquerer ProjectNKN
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadStart;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.Settings;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

/**
 * this class is used to communicate with the user using a console or streams
 * 
 * @author Patrick Hechler
 */
public class SquareConquererCUI implements Runnable {
	
	// help method constants
	private static final String ASK_RETRY_Y_ES_N_O                        = Messages.getString("SquareConquererCUI.ask~retry"); //$NON-NLS-1$
	private static final String READN_NUM_AND_THE_MAXIMUM_NUMBER_IS       = Messages.getString("SquareConquererCUI.read-num~max-num-addition"); //$NON-NLS-1$
	private static final String READN_NUM_THE_MINIMUM_NUMBER_IS_0_1       = Messages.getString("SquareConquererCUI.read-num~min-num-is"); //$NON-NLS-1$
	private static final String READN_NUM_COULD_NOT_PARSE_THE_NUMBER_0_1  = Messages.getString("SquareConquererCUI.read-num~invalid-number"); //$NON-NLS-1$
	private static final String LOAD_TASK_LOAD_NOW_THE_FILE_0             = Messages.getString("SquareConquererCUI.load-task~load-file"); //$NON-NLS-1$
	private static final String PROMPT_L_OAD_WORLD_OR_CREATE_A_N_EW_WORLD = Messages.getString("SquareConquererCUI.prompt~l_oad-or-create-n_ew-world"); //$NON-NLS-1$
	private static final String ERROR_WHILE_EXECUTING_TASK_0              = Messages.getString("SquareConquererCUI.error-while-exec-task"); //$NON-NLS-1$
	// help constants
	private static final String HELP_TOO_MANY_ARGS   = Messages.getString("SquareConquererCUI.help~too-many-args"); //$NON-NLS-1$
	private static final String HELP_HELP_MESSAGE    = Messages.getString("SquareConquererCUI.help~help"); //$NON-NLS-1$
	private static final String HELP_GENERAL_MESSAGE = Messages.getString("SquareConquererCUI.help~general-help"); //$NON-NLS-1$
	// version constants
	private static final String VERSION_HELP_0_BLA_BLA             = Messages.getString("SquareConquererCUI.version~help"); //$NON-NLS-1$
	private static final String VERSION_SQUARE_CONQUERER_VERSION_0 = Messages.getString("SquareConquererCUI.version~version"); //$NON-NLS-1$
	// status constants
	private static final String STATUS_USER_NAME_0                       = Messages.getString("SquareConquererCUI.status~user-name"); //$NON-NLS-1$
	private static final String STATUS_USER_NOT_LOGGED_IN_USERNAME_0     = Messages.getString("SquareConquererCUI.status~user-not-logged-in-name"); //$NON-NLS-1$
	private static final String STATUS_BOUNDS_XLEN_0_YLEN_1              = Messages.getString("SquareConquererCUI.status~world-bounds"); //$NON-NLS-1$
	private static final String STATUS_WORLD_UNKNOWN_TYPE_0              = Messages.getString("SquareConquererCUI.status~unknown-world-type"); //$NON-NLS-1$
	private static final String STATUS_WORLD_USER_WORLD_LOADED           = Messages.getString("SquareConquererCUI.status~user-world"); //$NON-NLS-1$
	private static final String STATUS_BOUNDS_NOT_LOADED                 = Messages.getString("SquareConquererCUI.status~bounds-not-loaded"); //$NON-NLS-1$
	private static final String STATUS_WORLD_BUILDER_WORLD_LOADED        = Messages.getString("SquareConquererCUI.status~build-world"); //$NON-NLS-1$
	private static final String STATUS_WORLD_ROOT_WORLD_LOADED           = Messages.getString("SquareConquererCUI.status~root-world"); //$NON-NLS-1$
	private static final String STATUS_WORLD_NO_WORLD                    = Messages.getString("SquareConquererCUI.status~no-world"); //$NON-NLS-1$
	private static final String STATUS_REMOTE_WORLD_SIZES_UPDATED        = Messages.getString("SquareConquererCUI.status~remote-world-sizes-updated"); //$NON-NLS-1$
	private static final String STATUS_MISSING_REMOTE_WORLD              = Messages.getString("SquareConquererCUI.status~no-remote-world"); //$NON-NLS-1$
	private static final String STATUS_SERVER_NO_SERVER                  = Messages.getString("SquareConquererCUI.status~no-server"); //$NON-NLS-1$
	private static final String STATUS_SERVER_NO_CONNECTS                = Messages.getString("SquareConquererCUI.status~none-(connects)"); //$NON-NLS-1$
	private static final String STATUS_SERVER_RUNNING                    = Messages.getString("SquareConquererCUI.status~server-n-connects"); //$NON-NLS-1$
	private static final String STATUS_SERVER_PW_NONE_BUT_SERVER_RUNNING = Messages.getString("SquareConquererCUI.status~no-server-pw-but-server-running"); //$NON-NLS-1$
	private static final String STATUS_SERVER_PW_THERE_IS_NONE           = Messages.getString("SquareConquererCUI.status~no-server-pw"); //$NON-NLS-1$
	private static final String STATUS_SERVER_PW_THERE_IS_ONE            = Messages.getString("SquareConquererCUI.status~server-pw-yes"); //$NON-NLS-1$
	private static final String STATUS_HELP_BLA_BLA                      = Messages.getString("SquareConquererCUI.status~help"); //$NON-NLS-1$
	private static final String USERNAME_ENTER_YOUR_NEW_USERNAME         = Messages.getString("SquareConquererCUI.username~enter-your-new"); //$NON-NLS-1$
	private static final String USERNAME_CURRENT_USERNAME_               = Messages.getString("SquareConquererCUI.username~your-current"); //$NON-NLS-1$
	private static final String USERNAME_HELP_CMD_0_HELP_1_SET_2_GET_3   = Messages.getString("SquareConquererCUI.username~help"); //$NON-NLS-1$
	// world constants
	private static final String WORLD_LOAD_THE_PATH_0_DOES_NOT_A_FILE        = Messages.getString("SquareConquererCUI.world~load~path-is-no-file"); //$NON-NLS-1$
	private static final String WORLD_LOAD_THE_FILE_0_DOES_NOT_EXIST         = Messages.getString("SquareConquererCUI.world~load~path-not-exist"); //$NON-NLS-1$
	private static final String WORLD_PRINT_GROUNDS_LEGEND                   = Messages.getString("SquareConquererCUI.world~print-ground~legend"); //$NON-NLS-1$
	private static final String WORLD_PRINT_SIZES                            = Messages.getString("SquareConquererCUI.world~print-size"); //$NON-NLS-1$
	private static final String WORLD_PRINT_RESOURCES_LEGEND                 = Messages.getString("SquareConquererCUI.world~print-resource~legend"); //$NON-NLS-1$
	private static final String WORLD_PRINT_MISSING_WORLD                    = Messages.getString("SquareConquererCUI.world~print~no-world"); //$NON-NLS-1$
	private static final String WORLD_LOAD_PROMPT_ENTER_FILE_TO_BE_LOADED    = Messages.getString("SquareConquererCUI.world~load~enter-load-file"); //$NON-NLS-1$
	private static final String WRITE_TILE_X_0_Y_1_GROUND_2_RESOURCE_3       = Messages.getString("SquareConquererCUI.world~print~single-tile"); //$NON-NLS-1$
	private static final String WORLD_NOARG_ENTER_Y_COORDINATE_OF_TILE       = Messages.getString("SquareConquererCUI.world~prompt~enter-x-coordinate"); //$NON-NLS-1$
	private static final String WORLD_NOARG_ENTER_X_COORDINATE_OF_TILE       = Messages.getString("SquareConquererCUI.world~prompt~enter-y-coordinate"); //$NON-NLS-1$
	private static final String WORLD_NOARG_DISPLAY_C_OMPLETE_OR_T_ILE       = Messages.getString("SquareConquererCUI.world~prompt~c_omplete-world-or-t_ile"); //$NON-NLS-1$
	private static final String WORLD_NOARG_CREATE_NEW_FINISH                = Messages.getString("SquareConquererCUI.world~created-world"); //$NON-NLS-1$
	private static final String WORLD_NOARG_PROMPT_ENTER_Y_LEN               = Messages.getString("SquareConquererCUI.world~prompt~enter-y-len"); //$NON-NLS-1$
	private static final String WORLD_NOARG_PROMPT_ENTER_X_LEN               = Messages.getString("SquareConquererCUI.world~prompt~enter-x-len"); //$NON-NLS-1$
	private static final String WORLD_NOARG_L_OAD_N_EW_OR_C_ANCEL            =
		Messages.getString("SquareConquererCUI.world~prompt~l_oad-file-or-n_ew-world-or-c_ancel"); //$NON-NLS-1$
	private static final String WORLD_NOARG_PROMPT_C_HAGE_OR_D_ISPLAY        = Messages.getString("SquareConquererCUI.world~prompt~c_hange-or-d_isplay"); //$NON-NLS-1$
	private static final String WORLD_TILE_RESOURCE_UNKNOWN_RESOURCE         = Messages.getString("SquareConquererCUI.world~unknown-resource-type"); //$NON-NLS-1$
	private static final String WORLD_TILE_TYPE_UNKNOWN_GROUND               = Messages.getString("SquareConquererCUI.world~unknown-ground-type"); //$NON-NLS-1$
	private static final String WORLD_TILE_TYPE_NOT_ACCEPT_SUFFIX            = Messages.getString("SquareConquererCUI.world~type-0-no-accepts-suffix-1"); //$NON-NLS-1$
	private static final String WORLD_MODIFY_NO_BUILD_WORLD                  = Messages.getString("SquareConquererCUI.world~world-not-in-build-mode"); //$NON-NLS-1$
	private static final String WORLD_MODIFY_MISSING_WORLD                   = Messages.getString("SquareConquererCUI.world~no-world-to-be-modified"); //$NON-NLS-1$
	private static final String WORLD_BUILD_ERROR_ON_BUILD                   = Messages.getString("SquareConquererCUI.world~build-failed"); //$NON-NLS-1$
	private static final String WORLD_BUILD_FINISH                           = Messages.getString("SquareConquererCUI.world~build-finish"); //$NON-NLS-1$
	private static final String WORLD_BUILD_NO_BUILD_WORLD                   = Messages.getString("SquareConquererCUI.world~only-build-can-be-build"); //$NON-NLS-1$
	private static final String WORLD_SAVE_FINISH                            = Messages.getString("SquareConquererCUI.world~save~finish"); //$NON-NLS-1$
	private static final String WORLD_SAVE__ALL_ERROR_ON_SAVE                = Messages.getString("SquareConquererCUI.world~save~error-on-save"); //$NON-NLS-1$
	private static final String WORLD_SAVE_ALL_FINISH                        = Messages.getString("SquareConquererCUI.world~save-all~finish"); //$NON-NLS-1$
	private static final String WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL = Messages.getString("SquareConquererCUI.world~save~file-exists-proceed"); //$NON-NLS-1$
	private static final String WORLD_SAVE_ALL_NOT_ROOT_WORLD                = Messages.getString("SquareConquererCUI.world~save-all~no-root-world"); //$NON-NLS-1$
	private static final String WORLD_SAVE_ALL_MISSING_WORLD                 = Messages.getString("SquareConquererCUI.world~save~no-world"); //$NON-NLS-1$
	private static final String WORLD_TO_BUILD_FINISH                        = Messages.getString("SquareConquererCUI.world~build~converted-to-build"); //$NON-NLS-1$
	private static final String WORLD_CONVERT_MISSING_WORLD                  = Messages.getString("SquareConquererCUI.world~convert~no-world"); //$NON-NLS-1$
	private static final String WORLD_TILE_MISSING_WORLD_TO_PRINT            = Messages.getString("SquareConquererCUI.world~print-tile~no-world"); //$NON-NLS-1$
	private static final String WORLD_CREATE_CLOSE_SERVER_THREAD             = Messages.getString("SquareConquererCUI.world~server~close-server-thread"); //$NON-NLS-1$
	private static final String WORLD_CREATE_ERROR_CLOSING_REMOTE_WORLD      = Messages.getString("SquareConquererCUI.world~server~error-on-close"); //$NON-NLS-1$
	private static final String WORLD_CREATE_COULD_NOT_PARSE_THE_WORLD_SIZE  = Messages.getString("SquareConquererCUI.world~create~unparsable-world-size"); //$NON-NLS-1$
	private static final String WORLD_CREATE_NOT_LOGGED_IN                   = Messages.getString("SquareConquererCUI.world~create~no-user"); //$NON-NLS-1$
	private static final String WORLD_LOAD_LOADED_WORLD_AND_USERS            = Messages.getString("SquareConquererCUI.world~load~finish"); //$NON-NLS-1$
	private static final String WORLD_LOAD_FINISH_LOAD                       = Messages.getString("SquareConquererCUI.world~load-all~finish"); //$NON-NLS-1$
	private static final String WORLD_LOAD_ERROR_ON_LOAD                     = Messages.getString("SquareConquererCUI.world~load~error-on-load"); //$NON-NLS-1$
	private static final String WORLD_LOAD_CHANGED_TO_ROOT                   = Messages.getString("SquareConquererCUI.world~load~changed-to-root"); //$NON-NLS-1$
	private static final String WORLD_LOAD_NOT_LOGGED_IN                     = Messages.getString("SquareConquererCUI.world~load~no-user"); //$NON-NLS-1$
	private static final String WORLD_LOAD_NO_REGULAR_FILE                   = Messages.getString("SquareConquererCUI.world~load~no-regular-file"); //$NON-NLS-1$
	private static final String WORLD_LOAD_FILE_NOT_EXIST                    = Messages.getString("SquareConquererCUI.world~load~path-not-exist2"); //$NON-NLS-1$
	private static final String WORLD_HELP_BLA_BLA_MNY_ARGS                  = Messages.getString("SquareConquererCUI.world~help"); //$NON-NLS-1$
	// server constants
	private static final String SERVER_STARTED_SERVER                           = Messages.getString("SquareConquererCUI.server~started"); //$NON-NLS-1$
	private static final String SERVER_SERVER_STOPPED_WITH_ERROR                = Messages.getString("SquareConquererCUI.server~error-on-stop-TODO-Messages.format"); //$NON-NLS-1$
	private static final String SERVER_CLOSED_MESSAGE                           = Messages.getString("SquareConquererCUI.server~stopped"); //$NON-NLS-1$
	private static final String SERVER_NOARG_ENTER_NOW_SERVER_PORT_DEFAULT_IS_0 = Messages.getString("SquareConquererCUI.server~enter-now-port"); //$NON-NLS-1$
	private static final String SERVER_NOARG_ERROR_DURING_CONNECT               = Messages.getString("SquareConquererCUI.server~error-on-connect"); //$NON-NLS-1$
	private static final String SERVER_NOARG_CONNECTED_TO_0_AT_PORT_1           = Messages.getString("SquareConquererCUI.server~connect~finish"); //$NON-NLS-1$
	private static final String SERVER_NOARG_PROMPT_ENTER_SERVERHOST            = Messages.getString("SquareConquererCUI.server~enter-host"); //$NON-NLS-1$
	private static final String SERVER_NOARG_P_ROCEED_OR_C_ANCEL                = Messages.getString("SquareConquererCUI.server~proceed-will-discard-current-world"); //$NON-NLS-1$
	private static final String SERVER_NOARG_S_TART_SERVER                      = Messages.getString("SquareConquererCUI.server~s_tart-own-server-addition"); //$NON-NLS-1$
	private static final String SERVER_NOARG_C_ONNECT_0_OR_N_OTHING             = Messages.getString("SquareConquererCUI.server~c_onnect-or-n_othing"); //$NON-NLS-1$
	private static final String SERVER_NOARG_NOT_LOGGED_IN                      = Messages.getString("SquareConquererCUI.server~no-user"); //$NON-NLS-1$
	private static final String SERVER_NOARG_ERROR_WHILE_DISCONNECT_NO_RETRY_0  = Messages.getString("SquareConquererCUI.server~error-on-disconnect"); //$NON-NLS-1$
	private static final String SERVER_NOARG_DISCONNECTED                       = Messages.getString("SquareConquererCUI.server~disconeccted"); //$NON-NLS-1$
	private static final String SERVER_NOARG_CONTAIN_CUR_WORLD_AS_BUILD_YN      = Messages.getString("SquareConquererCUI.server~contain-cur-world-as-build_yes/no"); //$NON-NLS-1$
	private static final String SERVER_NOARG_PROMPT_D_ISCONNECT_OR_N_OTHING     = Messages.getString("SquareConquererCUI.server~d_isconnect-or-n_othing"); //$NON-NLS-1$
	private static final String SERVER_NOARG_CONNECTE_TO_SERVER                 = Messages.getString("SquareConquererCUI.server~currently-connected-to-server"); //$NON-NLS-1$
	private static final String SERVER_NOARG_C_LOSE_SERVER_OR_N_OTHING          = Messages.getString("SquareConquererCUI.server~c_lose-server-or-n_othing"); //$NON-NLS-1$
	private static final String SERVER_NOARG_SERVER_IS_RUNNING                  = Messages.getString("SquareConquererCUI.server~server-is-running"); //$NON-NLS-1$
	private static final String SERVER_STOP_STOPPED                             = Messages.getString("SquareConquererCUI.server~stopped2"); //$NON-NLS-1$
	private static final String SERVER_STOP_TOLD_TO_STOP                        = Messages.getString("SquareConquererCUI.server~told-to-stop"); //$NON-NLS-1$
	private static final String SERVER_STOP_NO_SERVER                           = Messages.getString("SquareConquererCUI.server~no-server"); //$NON-NLS-1$
	private static final String SERVER_START_STARTED_SERVER                     = Messages.getString("SquareConquererCUI.server~started2"); //$NON-NLS-1$
	private static final String SERVER_START_ERROR_AT_SERVER_THREAD_0           = Messages.getString("SquareConquererCUI.server~error-on-server-thread"); //$NON-NLS-1$
	private static final String SERVER_START_USER_0_LOGGED_IN_FROM_1            = Messages.getString("SquareConquererCUI.server~notfy-user-log-in"); //$NON-NLS-1$
	private static final String SERVER_USER_0_DISCONNECTED                      = Messages.getString("SquareConquererCUI.server~notfy-user-disconnect"); //$NON-NLS-1$
	private static final String SERVER_START_NO_ROOT_WORLD                      = Messages.getString("SquareConquererCUI.server~start~no-root-world"); //$NON-NLS-1$
	private static final String SERVER_START_MISSING_WORLD                      = Messages.getString("SquareConquererCUI.server~start~no-world"); //$NON-NLS-1$
	private static final String SERVER_START_ALREADY_RUNNING                    = Messages.getString("SquareConquererCUI.server~start~already-started"); //$NON-NLS-1$
	private static final String SERVER_DISCONNECT_DISCONNECTING_ERROR_NO_RETRY  = Messages.getString("SquareConquererCUI.server~disconnect~error-on-disconnect"); //$NON-NLS-1$
	private static final String SERVER_DISCONNECT_CLOSED_REMOTE_WORLD           = Messages.getString("SquareConquererCUI.server~disconnect~closed-remote-world2"); //$NON-NLS-1$
	private static final String SERVER_DISCONNECT_NO_SERVER_CONNECTION          = Messages.getString("SquareConquererCUI.server~disconnect~no-connection"); //$NON-NLS-1$
	private static final String SERVER_CONNECT_ERROR_WHILE_CONNECTING_0         = Messages.getString("SquareConquererCUI.server~connect~error-on-connect"); //$NON-NLS-1$
	private static final String SERVER_COULD_NOT_PARSE_PORT_0                   = Messages.getString("SquareConquererCUI.server~connect~unparsable-port"); //$NON-NLS-1$
	private static final String SERVER_CONNECT_NOT_LOGGED_IN                    = Messages.getString("SquareConquererCUI.server~connect~no-user"); //$NON-NLS-1$
	private static final String SERVER_STATUS_NOTHING                           = Messages.getString("SquareConquererCUI.server~status~nothing"); //$NON-NLS-1$
	private static final String SERVER_STATS_CONNECTED                          = Messages.getString("SquareConquererCUI.server~status~remote-world"); //$NON-NLS-1$
	private static final String SERVER_STATUS_RUNNING                           = Messages.getString("SquareConquererCUI.server~status~connected"); //$NON-NLS-1$
	private static final String SERVER_HELP_BLA_BLA                             = Messages.getString("SquareConquererCUI.server~help");                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               // set //$NON-NLS-1$
																																		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               // password
																																		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               // constants
	private static final String SET_PW_PROMPT_ENTER_YOUR_PASSWORD               = Messages.getString("SquareConquererCUI.ser-pw~your-new-pw"); //$NON-NLS-1$
	private static final String SET_PW_PROMPT_ENTER_USER_PASSWORD               = Messages.getString("SquareConquererCUI.ser-pw~usrs-new-pw"); //$NON-NLS-1$
	private static final String SET_PW_PROMPT_ENTER_USERNAME                    = Messages.getString("SquareConquererCUI.ser-pw~enter-username"); //$NON-NLS-1$
	private static final String SET_PW_YOUR_PW_WAS_CHANGED                      = Messages.getString("SquareConquererCUI.ser-pw~your-pw-changed"); //$NON-NLS-1$
	private static final String SET_PW_PW_OF_0_CHANGED                          = Messages.getString("SquareConquererCUI.ser-pw~usrs-pw-changed"); //$NON-NLS-1$
	private static final String SET_PW_ONLY_ROOT_HAS_OTHR_USRS                  = Messages.getString("SquareConquererCUI.ser-pw~only-root-change-other"); //$NON-NLS-1$
	private static final String SET_PW_USER_0_NOT_FOUND                         = Messages.getString("SquareConquererCUI.ser-pw~usr-not-found"); //$NON-NLS-1$
	private static final String SET_PW_HELP_0_SET_OF_1_2_ME_3                   = Messages.getString("SquareConquererCUI.ser-pw~help"); //$NON-NLS-1$
	private static final String SET_PW_S_OMEONES_PASSWORD                       = Messages.getString("SquareConquererCUI.ser-pw~s_omeones-pw-addition"); //$NON-NLS-1$
	private static final String SET_PW_Y_OUR_PW_0_OR_DO_N_OTHING                = Messages.getString("SquareConquererCUI.ser-pw~set-y_our-pw-or-n_othing");                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // server //$NON-NLS-1$
	// PW
	// constants
	private static final String SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD    = Messages.getString("SquareConquererCUI.server-pw~enter-new"); //$NON-NLS-1$
	private static final String SERVER_PW_REMOVE_THERE_IS_NO_SERVER_PW        = Messages.getString("SquareConquererCUI.server-pw~no-server-pw-to-remove"); //$NON-NLS-1$
	private static final String SERVER_PW_STATUS_THERE_IS_SPW                 = Messages.getString("SquareConquererCUI.server-pw~there-is-server-pw"); //$NON-NLS-1$
	private static final String SERVER_PW_UPDATED_SERVER_PW                   = Messages.getString("SquareConquererCUI.server-pw~updated"); //$NON-NLS-1$
	private static final String SERVER_PW_STATUS_THERE_IS_NO_SPW              = Messages.getString("SquareConquererCUI.server-pw~no-server-pw"); //$NON-NLS-1$
	private static final String SERVER_PW_HELP_BLA_BLA                        = Messages.getString("SquareConquererCUI.server-pw~help"); //$NON-NLS-1$
	private static final String SERVER_PW_R_EMOVE                             = Messages.getString("SquareConquererCUI.server-pw~r_emove-addition"); //$NON-NLS-1$
	private static final String SERVER_PW_S_ET_0_THE_SERVER_PW_OR_DO_N_OTHING = Messages.getString("SquareConquererCUI.server-pw~s_et-server-pw-or-n_othing"); //$NON-NLS-1$
	// quit constants
	private static final String QUIT_ERROR_WHILE_PARSING_EXIT_NUMBER_0 = Messages.getString("SquareConquererCUI.quit~unparsable-exit-num"); //$NON-NLS-1$
	private static final String QUIT_HELP_BLA_BLA_HELP_WITH_0          = Messages.getString("SquareConquererCUI.quit~help"); //$NON-NLS-1$
	private static final String QUIT_GOODBYE_EXIT_NOW_WITH_0           = Messages.getString("SquareConquererCUI.quit~bye"); //$NON-NLS-1$
	// general constants
	private static final String UNKNOWN_GROUND_TYPE_0                             = Messages.getString("SquareConquererCUI.general~unknown-ground"); //$NON-NLS-1$
	private static final String UNKNOWN_TILE_RESOURCE_0                           = Messages.getString("SquareConquererCUI.general~unknown-resource"); //$NON-NLS-1$
	private static final String COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3 = Messages.getString("SquareConquererCUI.general~coordinate-out-of-bounds"); //$NON-NLS-1$
	private static final String INTERRUPT_ERROR_0                                 = Messages.getString("SquareConquererCUI.general~interrupted"); //$NON-NLS-1$
	private static final String NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG                 = Messages.getString("SquareConquererCUI.general~no-enugh-args"); //$NON-NLS-1$
	private static final String UNKNOWN_ARGUMENT_0                                = Messages.getString("SquareConquererCUI.general~unknow-arg"); //$NON-NLS-1$
	private static final String ERROR_WHILE_EXECUTING_THE_COMMAND_0               = Messages.getString("SquareConquererCUI.general~error-on-exec"); //$NON-NLS-1$
	private static final String UNKNOWN_COMMAND_0                                 = Messages.getString("SquareConquererCUI.general~unknown-command"); //$NON-NLS-1$
	private static final String GREET_BLA_BLA_HELP_WITH_0_COMMAND                 = Messages.getString("SquareConquererCUI.greet"); //$NON-NLS-1$
	private static final String THERE_IS_NO_CONS                                  = Messages.getString("SquareConquererCUI.no-cons"); //$NON-NLS-1$
	
	private static final String HELP = "help"; //$NON-NLS-1$
	
	private static final String CMD_HELP     = HELP;
	private static final String CMD_VERSION  = "version";  //$NON-NLS-1$
	private static final String CMD_STATUS   = "status";   //$NON-NLS-1$
	private static final String CMD_USERNAME = "username"; //$NON-NLS-1$
	private static final String CMD_WORLD    = "world";    //$NON-NLS-1$
	private static final String CMD_SERVER   = "server";   //$NON-NLS-1$
	private static final String CMD_SETPW    = "setpw";    //$NON-NLS-1$
	private static final String CMD_SERVERPW = "serverpw"; //$NON-NLS-1$
	private static final String CMD_QUIT     = "quit";     //$NON-NLS-1$
	private static final String CMD_EXIT     = "exit";     //$NON-NLS-1$
	
	private static final Pattern PTRN_ARG       = Pattern.compile("([^\\s\\\\'\"]+|'[^']*'|\"[^\"]*\")+");                  //$NON-NLS-1$
	private static final Pattern PTRN_STR       = Pattern.compile("[^\\\\]('([^'\\\\]*|\\\\.)*'|\"([^\"\\\\]*|\\\\.)*\")"); //$NON-NLS-1$
	private static final Pattern PTRN_BACKSLASH = Pattern.compile("\\\\(.)");                                               //$NON-NLS-1$
	
	private static List<String> arguments(String line) {
		List<String> args       = new ArrayList<>();
		Matcher      argMatcher = PTRN_ARG.matcher(line);
		while (argMatcher.find()) {
			String        arg        = argMatcher.group(0);
			Matcher       strMatcher = PTRN_STR.matcher(arg);
			StringBuilder b          = null;
			int           off        = 0;
			while (strMatcher.find()) {
				if (b == null) {
					b = new StringBuilder();
				}
				b.append(arg, off, strMatcher.start() + 1);
				String str = arg.substring(strMatcher.start() + 2, strMatcher.end() - 1);
				b.append(str);
				off = strMatcher.end();
			}
			if (b != null) {
				b.append(arg, off, arg.length());
				arg = b.toString();
			}
			Matcher bsMatcher = PTRN_BACKSLASH.matcher(arg);
			arg = bsMatcher.replaceAll("$1"); //$NON-NLS-1$
			args.add(arg);
		}
		return args;
	}
	
	private final Cons c;
	private boolean    interactive;
	
	private volatile Thread                serverThread;
	private volatile Map<User, Connection> connects;
	
	private User   usr;
	private World  world;
	private String username;
	private char[] serverPW;
	
	private List<Object> tasks = new LinkedList<>();
	
	/**
	 * this is equivalent to <code>{@link #SquareConquererCUI(Cons) new SquareConquererCU}({@link System#console()} != null ?
	 * {@link ConsoleCons#ConsoleCons(Console) new ConsoleCons}({@link System#console()}) :
	 * {@link IOCons#IOCons(Scanner, java.io.PrintStream) new IOCons}
	 * ({@link Scanner#Scanner(InputStream) new Scanner}({@link System#in}, {@link System#out})))</code>
	 */
	public SquareConquererCUI() {
		Console console = System.console();
		if (console != null) {
			this.c           = new ConsoleCons(console);
			this.interactive = true;
		} else {
			Scanner sc = new Scanner(System.in);
			this.c           = new IOCons(sc, System.out);
			this.interactive = false;
		}
	}
	
	/**
	 * this is equivalent to <code>{@link #SquareConquererCUI(Cons, boolean) new SquareConquererCUI}(c, c instanceof {@link ConsoleCons})</code>
	 * 
	 * @param c the {@link Cons} to be used
	 */
	public SquareConquererCUI(Cons c) {
		this(c, c instanceof ConsoleCons);
	}
	
	/**
	 * creates a new {@link SquareConquererCUI} using the given {@link Cons} and mode
	 * 
	 * @param c           the {@link Cons} to be used
	 * @param interactive if the {@link Cons} should be in interactive mode or not
	 */
	public SquareConquererCUI(Cons c, boolean interactive) {
		if (c == null) {
			throw new NullPointerException(THERE_IS_NO_CONS);
		}
		this.c           = c;
		this.interactive = interactive;
	}
	
	/**
	 * set the interactive mode
	 * 
	 * @param interactive the new interactive mode
	 */
	public void interactive(boolean interactive) { this.interactive = interactive; }
	
	/**
	 * sets the world, server thread and connections
	 * 
	 * @param world        the world
	 * @param serverThread the server thread or <code>null</code> if no server is running
	 * @param connects     the connections map or <code>null</code> if no server is running
	 */
	public void setWorld(World world, Thread serverThread, Map<User, Connection> connects) {
		this.world        = world;
		this.serverThread = serverThread;
		this.connects     = connects;
		this.usr          = world.user();
		if (serverThread != null) {
			threadStart(() -> {
				while (serverThread.isAlive()) {
					try {
						serverThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				synchronized (SquareConquererCUI.this) {
					if (this.serverThread == serverThread) {
						this.serverThread = null;
						this.connects     = null;
					}
				}
			});
		}
	}
	
	/**
	 * sets the logged in user
	 * 
	 * @param usr the new logged in user
	 */
	public void user(User usr) { this.usr = usr; }
	
	/**
	 * sets the name of the not logged in user (ignored if a user is logged in)
	 * 
	 * @param name the name of the not logged in user
	 */
	public void name(String name) { this.username = name; }
	
	/**
	 * sets the server password
	 * 
	 * @param serverpw the new server password
	 */
	public void serverPW(char[] serverpw) { this.serverPW = serverpw; }
	
	/**
	 * add a load world task
	 * 
	 * @param worldFile the file from which the world should be loaded
	 */
	public void startLoad(Path worldFile) { this.tasks.add(worldFile); }
	
	/**
	 * add a start server task
	 * 
	 * @param p the port of the server
	 */
	public void startServer(int p) { this.tasks.add(new StartServerTask(p)); }
	
	/**
	 * add a connect to server task
	 * 
	 * @param host the host name
	 * @param p    the server port
	 */
	public void startConnect(String host, int p) { this.tasks.add(new ConnectToServerTask(host, p)); }
	
	private static class StartServerTask {
		
		private final int port;
		
		private StartServerTask(int port) { this.port = port; }
		
	}
	
	private static class ConnectToServerTask {
		
		private final String host;
		private final int    port;
		
		private ConnectToServerTask(String host, int port) { this.host = host; this.port = port; }
		
	}
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		boolean greet = this.interactive;
		while (true) {
			doTasks();
			if (greet) {
				this.c.writeLines(MessageFormat.format(GREET_BLA_BLA_HELP_WITH_0_COMMAND, HELP));
				greet = false;
			}
			String line = (this.interactive ? this.c.readLine(prompt()) : this.c.readLine()).trim();
			if (line.isEmpty()) {
				continue;
			}
			List<String> args = arguments(line);
			if (args.isEmpty()) {
				this.c.writeLines("stange, there is nothing"); //$NON-NLS-1$ this should never happen
				continue;
			}
			exec(args);
		}
	}
	
	private void exec(List<String> args) {
		try {
			switch (args.get(0).toLowerCase()) {
			case CMD_HELP -> cmdHelp(args);
			case CMD_VERSION -> cmdVersion(args);
			case CMD_STATUS -> cmdStatus(args);
			case CMD_USERNAME -> cmdUsername(args);
			case CMD_WORLD -> cmdWorld(args);
			case CMD_SERVER -> cmdServer(args);
			case CMD_SETPW -> cmdSetPW(args);
			case CMD_SERVERPW -> cmdServerPW(args);
			case CMD_QUIT, CMD_EXIT -> cmdQuit(args);
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_COMMAND_0, args.get(0)));
			}
		} catch (RuntimeException e) {
			this.c.writeLines(MessageFormat.format(ERROR_WHILE_EXECUTING_THE_COMMAND_0, e));
		}
	}
	
	private void cmdQuit(List<String> args) {
		if (args.size() == 1) {
			if (this.interactive) {
				this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, "0")); //$NON-NLS-1$
			}
			System.exit(0);
		}
		for (int i = 1; i < args.size(); i++) {
			if (HELP.equalsIgnoreCase(args.get(i))) {
				this.c.writeLines(MessageFormat.format(QUIT_HELP_BLA_BLA_HELP_WITH_0, HELP));
			} else {
				try {
					int e = Integer.parseInt(args.get(i));
					this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, Integer.toString(e)));
					System.exit(e);
				} catch (NumberFormatException e) {
					this.c.writeLines(MessageFormat.format(QUIT_ERROR_WHILE_PARSING_EXIT_NUMBER_0, e));
					this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, "1")); //$NON-NLS-1$
					System.exit(1);
				}
			}
		}
	}
	
	private void cmdServerPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask(MessageFormat.format(SERVER_PW_S_ET_0_THE_SERVER_PW_OR_DO_N_OTHING, (this.serverPW != null ? SERVER_PW_R_EMOVE : "")), //$NON-NLS-1$
				this.serverPW != null ? "srn" : "sn")) { //$NON-NLS-1$ //$NON-NLS-2$
			case 's' -> this.serverPW = this.c.readPassword(SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD);
			case 'r' -> this.serverPW = null;
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$ this should never happen
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argStatus       = "status";         //$NON-NLS-1$
			final String argSet          = "set";            //$NON-NLS-1$
			final String argRemove       = "remove";         //$NON-NLS-1$
			final String argRemoveNoFail = "remove-no-fail"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SERVER_PW_HELP_BLA_BLA, argStatus, argSet, argRemove, argRemoveNoFail));
			}
			case argStatus -> {
				if (this.serverPW != null) {
					this.c.writeLines(SERVER_PW_STATUS_THERE_IS_SPW);
				} else {
					this.c.writeLines(SERVER_PW_STATUS_THERE_IS_NO_SPW);
				}
			}
			case argSet -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argSet));
					return;
				}
				this.serverPW = this.c.readPassword(SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD);
				this.c.writeLines(SERVER_PW_UPDATED_SERVER_PW);
			}
			case argRemove -> {
				if (this.serverPW == null) {
					this.c.writeLines(SERVER_PW_REMOVE_THERE_IS_NO_SERVER_PW);
					return;
				}
				this.serverPW = null;
			}
			case argRemoveNoFail -> this.serverPW = null;
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void cmdSetPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask(MessageFormat.format(SET_PW_Y_OUR_PW_0_OR_DO_N_OTHING, (this.usr instanceof RootUser ? SET_PW_S_OMEONES_PASSWORD : "")), //$NON-NLS-1$
				this.usr instanceof RootUser ? "ysn" : "yn")) { //$NON-NLS-1$ //$NON-NLS-2$
			case 'y' -> setMyPW();
			case 's' -> {
				String name = this.c.readLine(SET_PW_PROMPT_ENTER_USERNAME);
				User   user = ((RootUser) this.usr).get(name);
				if (user == null) {
					this.c.writeLines(MessageFormat.format(SET_PW_USER_0_NOT_FOUND, name));
					return;
				}
				char[] npw = this.c.readPassword(SET_PW_PROMPT_ENTER_USER_PASSWORD);
				((RootUser) this.usr).changePW(user, npw);
				this.c.writeLines(MessageFormat.format(SET_PW_PW_OF_0_CHANGED, name));
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argSet = "set"; //$NON-NLS-1$
			final String argOf  = "of";  //$NON-NLS-1$
			final String argMe  = "me";  //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SET_PW_HELP_0_SET_OF_1_2_ME_3, HELP, argSet, argOf, argMe, argSet, argOf, argMe));
			}
			case argSet, argOf -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argSet + "/" + argOf)); //$NON-NLS-1$
					return;
				}
				setPW(args.get(i));
			}
			case argMe -> setMyPW();
			default -> setPW(args.get(i));
			}
		}
	}
	
	private void setPW(String arg) {
		if (arg.equals(this.username) || arg.equals(this.usr == null ? RootUser.ROOT_NAME : this.usr.name())) {
			setMyPW();
		} else if (!(this.usr instanceof RootUser root)) {
			this.c.writeLines(SET_PW_ONLY_ROOT_HAS_OTHR_USRS);
		} else {
			setOtherPW(arg, root);
		}
	}
	
	private void setOtherPW(String name, RootUser root) {
		User user = root.get(name);
		if (user == null) {
			this.c.writeLines(MessageFormat.format(SET_PW_USER_0_NOT_FOUND, name));
		}
		char[] npw = this.c.readPassword(SET_PW_PROMPT_ENTER_USER_PASSWORD);
		((RootUser) this.usr).changePW(user, npw);
		this.c.writeLines(MessageFormat.format(SET_PW_PW_OF_0_CHANGED, name));
	}
	
	private void setMyPW() {
		char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
		if (this.username != null) {
			this.usr = User.create(this.username, pw);
		} else if (this.usr == null) {
			this.usr = RootUser.create(pw);
		} else {
			this.usr.changePassword(pw);
		}
		this.c.writeLines(SET_PW_YOUR_PW_WAS_CHANGED);
	}
	
	private void cmdServer(List<String> args) {
		if (args.size() == 1) {
			cmdServerNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argStatus     = "status";     //$NON-NLS-1$
			final String argConnect    = "connect";    //$NON-NLS-1$
			final String argDisconnect = "disconnect"; //$NON-NLS-1$
			final String argStart      = "start";      //$NON-NLS-1$
			final String argStop       = "stop";       //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SERVER_HELP_BLA_BLA, argStatus, argConnect, argDisconnect, argStart, "" + Connection.DEFAULT_PORT, argStop)); //$NON-NLS-1$
			}
			case argStatus -> {
				if (this.serverThread != null) {
					this.c.writeLines(SERVER_STATUS_RUNNING);
				} else if (this.world instanceof RemoteWorld) {
					this.c.writeLines(SERVER_STATS_CONNECTED);
				} else {
					this.c.writeLines(SERVER_STATUS_NOTHING);
				}
			}
			case argConnect -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argConnect));
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(MessageFormat.format(SERVER_CONNECT_NOT_LOGGED_IN, CMD_SETPW, CMD_USERNAME));
					return;
				}
				int    port = Connection.DEFAULT_PORT;
				String addr = args.get(i);
				int    li   = addr.lastIndexOf(':');
				if (li != -1 && li - 1 == addr.lastIndexOf(']') && addr.charAt(0) == '[') {
					try {
						port = Integer.parseInt(addr.substring(li + 1));
						addr = addr.substring(1, li - 1);
					} catch (NumberFormatException e) {
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
				try {
					Connection conn;
					if (this.serverPW != null) {
						conn = Connection.ClientConnect.connectNew(addr, port, this.usr, this.serverPW);
					} else {
						conn = Connection.ClientConnect.connect(addr, port, this.usr);
					}
					this.world = new RemoteWorld(conn);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_CONNECT_ERROR_WHILE_CONNECTING_0, e));
					return;
				}
			}
			case argDisconnect -> {
				if (!(this.world instanceof RemoteWorld rw)) {
					this.c.writeLines(SERVER_DISCONNECT_NO_SERVER_CONNECTION);
					return;
				}
				this.world = null;
				try {
					rw.close();
					this.c.writeLines(SERVER_DISCONNECT_CLOSED_REMOTE_WORLD);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_DISCONNECT_DISCONNECTING_ERROR_NO_RETRY, e));
				}
			}
			case argStart -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argStart));
					return;
				}
				synchronized (this) {
					if (this.serverThread != null) {
						this.c.writeLines(SERVER_START_ALREADY_RUNNING);
						return;
					}
					if (this.world == null) {
						this.c.writeLines(SERVER_START_MISSING_WORLD);
						return;
					}
					if (!(this.world instanceof RootWorld rw)) {
						this.c.writeLines(SERVER_START_NO_ROOT_WORLD);
						return;
					}
					try {
						int    port = "-".equals(args.get(i)) ? Connection.DEFAULT_PORT : Integer.parseInt(args.get(i)); //$NON-NLS-1$
						char[] spw  = this.serverPW;
						this.serverPW = null;
						Map<User, Connection> cs = new HashMap<>();
						this.connects     = cs;
						this.serverThread = threadStart(() -> {
												try {
													Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
																			if (sok == null) {
																				this.c.writeLines(MessageFormat.format(SERVER_USER_0_DISCONNECTED, conn.usr.name()));
																			} else {
																				this.c.writeLines(MessageFormat.format(SERVER_START_USER_0_LOGGED_IN_FROM_1,
																					conn.usr.name(), sok.getInetAddress()));
																			}
																		},
														cs, spw);
												} catch (IOException e) {
													this.c.writeLines(MessageFormat.format(SERVER_START_ERROR_AT_SERVER_THREAD_0, e));
												} finally {
													synchronized (SquareConquererCUI.this) {
														if (this.serverThread == Thread.currentThread()) {
															this.serverThread = null;
															this.connects     = null;
														}
													}
												}
											});
						this.c.writeLines(SERVER_START_STARTED_SERVER);
					} catch (NumberFormatException e) {
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
			}
			case argStop -> {
				Thread st = this.serverThread;
				if (st == null) {
					this.c.writeLines(SERVER_STOP_NO_SERVER);
					return;
				}
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
				}
				if (st.isAlive()) {
					this.c.writeLines(SERVER_STOP_TOLD_TO_STOP);
				} else {
					this.c.writeLines(SERVER_STOP_STOPPED);
				}
				closeConnections();
			}
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void cmdServerNoArgs() {
		Thread st = this.serverThread;
		if (st != null) {
			this.c.writeLines(SERVER_NOARG_SERVER_IS_RUNNING);
			if (ask(SERVER_NOARG_C_LOSE_SERVER_OR_N_OTHING, "cn") == 'c') { //$NON-NLS-1$
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
				}
				closeConnections();
				if (st.isAlive()) {
					this.c.writeLines(SERVER_STOP_TOLD_TO_STOP);
				} else {
					this.c.writeLines(SERVER_CLOSED_MESSAGE);
				}
			}
		} else if (this.world instanceof RemoteWorld rw) {
			this.c.writeLines(SERVER_NOARG_CONNECTE_TO_SERVER);
			if (ask(SERVER_NOARG_PROMPT_D_ISCONNECT_OR_N_OTHING, "dn") == 'd') { //$NON-NLS-1$
				World nw = null;
				if (ask(SERVER_NOARG_CONTAIN_CUR_WORLD_AS_BUILD_YN, "yn") == 'y') { //$NON-NLS-1$
					int               xlen = rw.xlen();
					int               ylen = rw.ylen();
					RootWorld.Builder b    = new RootWorld.Builder(this.usr.rootClone(), xlen, ylen);
					for (int x = 0; x < xlen; x++) {
						for (int y = 0; y < ylen; y++) {
							b.set(x, y, this.world.tile(x, y));
						}
					}
					nw = b;
				}
				try {
					rw.close();
					this.c.writeLines(SERVER_NOARG_DISCONNECTED);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_ERROR_WHILE_DISCONNECT_NO_RETRY_0, e));
				}
				this.world = nw;
			}
		} else if (this.usr == null) {
			this.c.writeLines(MessageFormat.format(SERVER_NOARG_NOT_LOGGED_IN, CMD_SETPW, CMD_USERNAME));
		} else {
			switch (ask(MessageFormat.format(SERVER_NOARG_C_ONNECT_0_OR_N_OTHING, (this.world instanceof RootWorld ? SERVER_NOARG_S_TART_SERVER : "")), //$NON-NLS-1$
				this.world instanceof RootWorld ? "csn" : "cn")) { //$NON-NLS-1$ //$NON-NLS-2$
			case 'c' -> {
				if (this.world != null && ask(SERVER_NOARG_P_ROCEED_OR_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					return;
				}
				String host = this.c.readLine(SERVER_NOARG_PROMPT_ENTER_SERVERHOST).trim();
				int    port = Connection.DEFAULT_PORT;
				int    li   = host.lastIndexOf(':');
				if (li != -1 && li - 1 == host.lastIndexOf(']') && host.charAt(0) == '[') {
					try {
						port = Integer.parseInt(host.substring(li + 1));
						host = host.substring(1, li - 1);
					} catch (NumberFormatException e) {
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
				Connection conn;
				try {
					char[] sp = this.serverPW;
					if (sp != null) {
						this.serverPW = null;
						conn          = Connection.ClientConnect.connectNew(host, port, this.usr, sp);
					} else {
						conn = Connection.ClientConnect.connect(host, port, this.usr);
					}
					this.world = new RemoteWorld(conn);
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_CONNECTED_TO_0_AT_PORT_1, host, Integer.toString(port)));
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_ERROR_DURING_CONNECT, e));
				}
			}
			case 's' -> {
				RootWorld rw      = (RootWorld) this.world;
				String    portStr = this.c.readLine(MessageFormat.format(SERVER_NOARG_ENTER_NOW_SERVER_PORT_DEFAULT_IS_0, "" + Connection.DEFAULT_PORT)).trim(); //$NON-NLS-1$
				int       port    = portStr.isEmpty() ? Connection.DEFAULT_PORT : Integer.parseInt(portStr);
				char[]    sp      = this.serverPW;
				this.serverPW = null;
				Map<User, Connection> cs = new HashMap<>();
				this.connects = cs;
				threadStart(() -> {
					try {
						Map<User, Connection> cs0 = new HashMap<>();
						this.connects = cs0;
						Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
							if (sok == null) {
								this.c.writeLines(MessageFormat.format(SERVER_USER_0_DISCONNECTED, conn.usr.name()));
							} else {
								this.c.writeLines(MessageFormat.format(SERVER_START_USER_0_LOGGED_IN_FROM_1, conn.usr.name(), sok.getInetAddress()));
							}
						}, cs0, sp);
					} catch (IOException e) {
						this.c.writeLines(SERVER_SERVER_STOPPED_WITH_ERROR + e);
					}
				});
				this.c.writeLines(SERVER_STARTED_SERVER);
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$ this should never happen
			}
		}
	}
	
	private void cmdWorld(List<String> args) {
		if (args.size() == 1) {
			cmdWorldNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argLoadAll           = "load-all";            //$NON-NLS-1$
			final String argLoad              = "load";                //$NON-NLS-1$
			final String argCreate            = "create";              //$NON-NLS-1$
			final String argPrint             = "print";               //$NON-NLS-1$
			final String argPrintTypes        = "print.types";         //$NON-NLS-1$
			final String argPrintResources    = "print.resources";     //$NON-NLS-1$
			final String argTile              = "tile";                //$NON-NLS-1$
			final String argToBuild           = "to-build";            //$NON-NLS-1$
			final String argSaveAll           = "save-all";            //$NON-NLS-1$
			final String argSaveAllForce      = "save-all-force";      //$NON-NLS-1$
			final String argSave              = "save";                //$NON-NLS-1$
			final String argSaveForce         = "save-force";          //$NON-NLS-1$
			final String argBuild             = "build";               //$NON-NLS-1$
			final String argTileType          = "tile.type";           //$NON-NLS-1$
			final String argTileResource      = "tile.resource";       //$NON-NLS-1$
			final String argFillRandom        = "fill-random";         //$NON-NLS-1$
			final String argFillTotallyRandom = "fill-totally-random"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(WORLD_HELP_BLA_BLA_MNY_ARGS, CMD_WORLD, HELP, argLoadAll, argLoad, argCreate, argPrint, argPrintResources,
					argTile, argToBuild, argSave, argSaveForce, argSaveAll, argSaveAllForce, argBuild, argTileType, argTileResource, argFillRandom,
					argFillTotallyRandom, argFillTotallyRandom));
			}
			case argLoadAll -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argLoadAll));
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLines(WORLD_LOAD_FILE_NOT_EXIST);
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLines(WORLD_LOAD_NO_REGULAR_FILE);
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(WORLD_LOAD_NOT_LOGGED_IN);
					return;
				}
				if (!(this.usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				this.usr      = this.usr.makeRoot();
				this.username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr)) {
					this.world = RootWorld.loadEverything(conn);
					this.c.writeLines(WORLD_LOAD_FINISH_LOAD);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
				}
			}
			case argLoad -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argLoad));
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLines(WORLD_LOAD_FILE_NOT_EXIST);
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLines(WORLD_LOAD_NO_REGULAR_FILE);
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(WORLD_LOAD_NOT_LOGGED_IN);
					return;
				}
				if (!(this.usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				this.usr      = this.usr.makeRoot();
				this.username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr)) {
					((RootUser) this.usr).load(conn);
					Tile[][] tiles = RemoteWorld.loadWorld(conn, ((RootUser) this.usr).users());
					this.world = RootWorld.Builder.createBuilder((RootUser) this.usr, tiles);
					this.c.writeLines(WORLD_LOAD_LOADED_WORLD_AND_USERS);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
				}
			}
			case argCreate -> {
				if (this.usr == null) {
					this.c.writeLines(WORLD_CREATE_NOT_LOGGED_IN);
					return;
				}
				i += 2;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argCreate));
					return;
				}
				int xlen;
				int ylen;
				try {
					xlen = Integer.parseInt(args.get(i - 1));
					ylen = Integer.parseInt(args.get(i));
				} catch (NumberFormatException nfe) {
					this.c.writeLines(MessageFormat.format(WORLD_CREATE_COULD_NOT_PARSE_THE_WORLD_SIZE, args.get(i - 1), args.get(i), nfe));
					return;
				}
				if (this.world instanceof RemoteWorld rw) {
					try {
						rw.close();
					} catch (IOException e) {
						this.c.writeLines(MessageFormat.format(WORLD_CREATE_ERROR_CLOSING_REMOTE_WORLD, e));
					}
				}
				this.world = null;
				Thread st = this.serverThread;
				if (st != null) {
					st.interrupt();
					this.c.writeLines(WORLD_CREATE_CLOSE_SERVER_THREAD);
					try {
						st.join(1000L);
					} catch (InterruptedException e) {
						this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
					}
				}
				closeConnections();
				this.world = new RootWorld.Builder(this.usr.makeRoot(), xlen, ylen);
				this.usr   = this.world.user();
			}
			case argPrint, argPrintTypes -> cmdWorldAllTilesType();
			case argPrintResources -> cmdWorldAllTilesResources();
			case argTile -> {
				i += 2;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTile));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_TILE_MISSING_WORLD_TO_PRINT);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				writeTile(x, y);
			}
			case argToBuild -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_CONVERT_MISSING_WORLD);
					return;
				}
				World old = this.world;
				if (old instanceof RemoteWorld rw) {
					if (!rw.getWorld()) {
						rw.getWorld(true);
						rw.needUpdate();
					} // ensure the complete world is loaded
					rw.tile(0, 0); // so the old user can be deleted (makeRoot)
				}
				int xlen = old.xlen();
				int ylen = old.ylen();
				this.usr = this.usr.makeRoot();
				RootWorld.Builder b = new RootWorld.Builder((RootUser) this.usr, xlen, ylen);
				for (int x = 0; x < xlen; x++) {
					for (int y = 0; y < ylen; y++) {
						b.set(x, y, old.tile(x, y));
					}
				}
				this.world = b;
				this.c.writeLines(WORLD_TO_BUILD_FINISH);
			}
			case argSaveAll, argSaveAllForce -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, args.get(i - 1)));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_SAVE_ALL_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld rw)) {
					this.c.writeLines(WORLD_SAVE_ALL_NOT_ROOT_WORLD);
					return;
				}
				Path p = Path.of(args.get(i));
				if (!argSaveAllForce.equals(args.get(i - 1)) && Files.exists(p) && ask(WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, this.usr)) {
					rw.saveEverything(conn);
					this.c.writeLines(WORLD_SAVE_ALL_FINISH);
				} catch (IOException e) {
					this.c.writeLines(WORLD_SAVE__ALL_ERROR_ON_SAVE + e.toString());
				}
			}
			case argSave, argSaveForce -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, args.get(i - 1)));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_SAVE_ALL_MISSING_WORLD);
					return;
				}
				Path p = Path.of(args.get(i));
				if (!argSaveForce.equals(args.get(i - 1)) && Files.exists(p) && ask(WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, this.usr)) {
					if (this.usr instanceof RootUser ru) {
						ru.save(conn);
					} else {
						try (RootUser tmp = RootUser.create(new char[0])) {
							tmp.save(conn);
						}
					}
					OpenWorld.saveWorld(this.world, conn);
					this.c.writeLines(WORLD_SAVE_FINISH);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_SAVE__ALL_ERROR_ON_SAVE, e));
				}
			}
			case argBuild -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_CONVERT_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_BUILD_NO_BUILD_WORLD);
					return;
				}
				try {
					this.world = b.create();
					this.c.writeLines(WORLD_BUILD_FINISH);
				} catch (IllegalStateException | NullPointerException e) {
					this.c.writeLines(MessageFormat.format(WORLD_BUILD_ERROR_ON_BUILD, e));
				}
			}
			case argTileType -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTileType));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				final String normalSuffix = "+normal"; //$NON-NLS-1$
				final String deepSuffix   = "+deep";   //$NON-NLS-1$
				final String hillSuffix   = "+hill";   //$NON-NLS-1$
				
				GroundType type = switch (args.get(i - 2).toLowerCase()) {
				case "not-explored" -> GroundType.NOT_EXPLORED; //$NON-NLS-1$
				case "water", "water+normal" -> GroundType.WATER_NORMAL; //$NON-NLS-1$ //$NON-NLS-2$
				case "sand", "sand+normal" -> GroundType.SAND; //$NON-NLS-1$ //$NON-NLS-2$
				case "grass", "grass+normal" -> GroundType.GRASS; //$NON-NLS-1$ //$NON-NLS-2$
				case "forest", "forest+normal" -> GroundType.FOREST; //$NON-NLS-1$ //$NON-NLS-2$
				case "swamp", "swamp+normal" -> GroundType.SWAMP; //$NON-NLS-1$ //$NON-NLS-2$
				case "mountain", "mountain+normal" -> GroundType.MOUNTAIN; //$NON-NLS-1$ //$NON-NLS-2$
				case normalSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || old.ground == GroundType.NOT_EXPLORED) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_NOT_ACCEPT_SUFFIX, old == null ? GroundType.NOT_EXPLORED : old.ground, normalSuffix));
						yield null;
					}
					yield old.ground.addNormal(false, true);
				}
				case "water+deep" -> GroundType.WATER_DEEP; //$NON-NLS-1$
				case deepSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isWater()) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_NOT_ACCEPT_SUFFIX, old == null ? GroundType.NOT_EXPLORED : old.ground, deepSuffix));
						yield null;
					}
					yield GroundType.WATER_DEEP;
				}
				case "sand+hill" -> GroundType.SAND_HILL; //$NON-NLS-1$
				case "grass+hill" -> GroundType.GRASS_HILL; //$NON-NLS-1$
				case "forest+hill" -> GroundType.FOREST_HILL; //$NON-NLS-1$
				case "swamp+hill" -> GroundType.SWAMP_HILL; //$NON-NLS-1$
				case hillSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isHill() && !old.ground.isFlat()) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_NOT_ACCEPT_SUFFIX, old == null ? GroundType.NOT_EXPLORED : old.ground, hillSuffix));
						yield null;
					}
					yield old.ground.addHill(false, true);
				}
				default -> {
					this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_UNKNOWN_GROUND, args.get(i - 2)));
					yield null;
				}
				};
				if (type != null) {
					b.set(x, y, type);
				}
			}
			case argTileResource -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTileResource));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				OreResourceType res = switch (args.get(i - 2).toLowerCase()) {
				case "none" -> OreResourceType.NONE; //$NON-NLS-1$
				case "gold" -> OreResourceType.GOLD_ORE; //$NON-NLS-1$
				case "iron" -> OreResourceType.IRON_ORE; //$NON-NLS-1$
				case "coal" -> OreResourceType.COAL_ORE; //$NON-NLS-1$
				default -> {
					this.c.writeLines(MessageFormat.format(WORLD_TILE_RESOURCE_UNKNOWN_RESOURCE, args.get(i - 2)));
					yield null;
				}
				};
				if (res != null) {
					b.set(x, y, res);
				}
			}
			case argFillRandom -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				b.fillRandom();
			}
			case argFillTotallyRandom -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				b.fillTotallyRandom();
			}
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void closeConnections() {
		Map<User, Connection> cs = this.connects;
		if (cs != null) {
			for (Entry<User, Connection> e : cs.entrySet()) {
				try {
					e.getValue().logOut();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getKey().close();
			}
		}
	}
	
	private void cmdWorldNoArgs() {
		if (this.world == null || ask(WORLD_NOARG_PROMPT_C_HAGE_OR_D_ISPLAY, "cd") == 'c') { //$NON-NLS-1$
			switch (ask(WORLD_NOARG_L_OAD_N_EW_OR_C_ANCEL, "lnc")) { //$NON-NLS-1$
			case 'l' -> cmdWorldInteractiveLoad();
			case 'n' -> {
				rootLogin(this.usr == null);
				int xlen = readNumber(WORLD_NOARG_PROMPT_ENTER_X_LEN, 1, Integer.MAX_VALUE);
				if (xlen > 0) {
					int ylen = readNumber(WORLD_NOARG_PROMPT_ENTER_Y_LEN, 1, Integer.MAX_VALUE);
					if (ylen > 0) {
						this.world = new RootWorld.Builder((RootUser) this.usr, xlen, ylen);
						this.c.writeLines(WORLD_NOARG_CREATE_NEW_FINISH);
					}
				}
			}
			case 'c' -> { return; }
			default -> throw new AssertionError("illegal return value from ask!"); //$NON-NLS-1$ this should never happen
			}
		} else if (ask(WORLD_NOARG_DISPLAY_C_OMPLETE_OR_T_ILE, "ct") == 'c') { //$NON-NLS-1$
			cmdWorldAllTilesType();
		} else {
			int x = readNumber(WORLD_NOARG_ENTER_X_COORDINATE_OF_TILE, 0, this.world.xlen() - 1);
			if (x >= 0) {
				int y = readNumber(WORLD_NOARG_ENTER_Y_COORDINATE_OF_TILE, 0, this.world.ylen() - 1);
				if (y >= 0) {
					writeTile(x, y);
				}
			}
		}
	}
	
	private void writeTile(int x, int y) {
		Tile tile = this.world.tile(x, y);
		this.c.writeLines(MessageFormat.format(WRITE_TILE_X_0_Y_1_GROUND_2_RESOURCE_3, Integer.toString(x), Integer.toString(y), tile.ground, tile.resource));
	}
	
	private void cmdWorldInteractiveLoad() {
		boolean askPW = this.usr == null;
		do {
			Path p;
			do {
				p = Path.of(this.c.readLine(WORLD_LOAD_PROMPT_ENTER_FILE_TO_BE_LOADED));
			} while (retry(!Files.exists(p) || !Files.isRegularFile(p)));
			if (!Files.exists(p)) {
				this.c.writeLines(MessageFormat.format(WORLD_LOAD_THE_FILE_0_DOES_NOT_EXIST, p));
				continue;
			}
			if (!Files.isRegularFile(p)) {
				this.c.writeLines(MessageFormat.format(WORLD_LOAD_THE_PATH_0_DOES_NOT_A_FILE, p));
				continue;
			}
			rootLogin(askPW);
			try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr)) {
				((RootUser) this.usr).load(conn);
				Tile[][] tiles = RemoteWorld.loadWorld(conn, ((RootUser) this.usr).users());
				this.world = RootWorld.Builder.createBuilder((RootUser) this.usr, tiles);
				this.c.writeLines(WORLD_LOAD_FINISH_LOAD);
			} catch (IOException e) {
				this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
			}
		} while (retry(true));
	}
	
	private void rootLogin(boolean askPW) {
		if (askPW) {
			char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
			if (this.usr != null && !(this.usr instanceof RootUser)) {
				this.username = ""; //$NON-NLS-1$ just for the log msg
			}
			this.usr = RootUser.create(pw);
			if (this.username != null) {
				this.username = null;
				this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
			}
		} else {
			this.usr = this.usr.makeRoot();
		}
	}
	
	@SuppressWarnings("preview")
	private void cmdWorldAllTilesResources() {
		if (this.world == null) {
			this.c.writeLines(WORLD_PRINT_MISSING_WORLD);
			return;
		}
		this.c.writeLines(MessageFormat.format(WORLD_PRINT_RESOURCES_LEGEND, "G", "I", "C")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		int xlen = this.world.xlen();
		int ylen = this.world.ylen();
		this.c.writeLines(MessageFormat.format(WORLD_PRINT_SIZES, Integer.toString(xlen), Integer.toString(ylen)));
		for (int y = 0; y < ylen; y++) {
			StringBuilder b = new StringBuilder(xlen);
			for (int x = 0; x < xlen; x++) {
				Tile t = this.world.tile(x, y);
				b.append(switch (t.resource) {
				case Object o when o == OreResourceType.NONE -> ' ';
				case Object o when o == OreResourceType.GOLD_ORE -> 'G';
				case Object o when o == OreResourceType.IRON_ORE -> 'I';
				case Object o when o == OreResourceType.COAL_ORE -> 'C';
				default -> throw new AssertionError(MessageFormat.format(UNKNOWN_TILE_RESOURCE_0, t.resource.name()));
				});
			}
			this.c.writeLines(b.toString());
		}
	}
	
	private void cmdWorldAllTilesType() {
		if (this.world == null) {
			this.c.writeLines(WORLD_PRINT_MISSING_WORLD);
			return;
		}
		this.c.writeLines(MessageFormat.format(WORLD_PRINT_GROUNDS_LEGEND, "#", "w", "W", "b", "B", "g", "G", "f", "F", "s", "S", "m")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
		int xlen = this.world.xlen();
		int ylen = this.world.ylen();
		this.c.writeLines(MessageFormat.format(WORLD_PRINT_SIZES, Integer.toString(xlen), Integer.toString(ylen)));
		for (int y = 0; y < ylen; y++) {
			StringBuilder b = new StringBuilder(xlen);
			for (int x = 0; x < xlen; x++) {
				Tile t = this.world.tile(x, y);
				b.append(switch (t.ground) {
				case NOT_EXPLORED -> '#';
				case WATER_NORMAL -> 'w';
				case WATER_DEEP -> 'W';
				case SAND -> 'b';
				case SAND_HILL -> 'B';
				case GRASS -> 'g';
				case GRASS_HILL -> 'G';
				case FOREST -> 'f';
				case FOREST_HILL -> 'F';
				case SWAMP -> 's';
				case SWAMP_HILL -> 'S';
				case MOUNTAIN -> 'm';
				default -> throw new AssertionError(MessageFormat.format(UNKNOWN_GROUND_TYPE_0, t.ground.name()));
				});
			}
			this.c.writeLines(b.toString());
		}
	}
	
	private void cmdUsername(List<String> args) {
		if (args.size() == 1) {
			cmdUsernameNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argSet = "set"; //$NON-NLS-1$
			final String argGet = "get"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(USERNAME_HELP_CMD_0_HELP_1_SET_2_GET_3, CMD_USERNAME, HELP, argSet, argGet));
			}
			case argSet -> {
				i++;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argSet));
					return;
				}
				String cur = args.get(i);
				if (this.usr == null) {
					this.username = RootUser.ROOT_NAME.equals(cur) ? null : cur;
				} else if (RootUser.ROOT_NAME.equals(cur)) {
					this.usr = this.usr.makeRoot();
				} else {
					this.usr = this.usr.changeName(cur);
				}
			}
			case argGet -> {
				String cur = this.usr == null ? this.username : this.usr.name();
				cur = cur == null ? RootUser.ROOT_NAME : cur;
				this.c.writeLines(cur);
			}
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void cmdUsernameNoArgs() {
		String cur = this.usr == null ? this.username : this.usr.name();
		if (cur != null) {
			this.c.writeLines(MessageFormat.format(USERNAME_CURRENT_USERNAME_, cur));
		}
		cur = this.c.readLine(USERNAME_ENTER_YOUR_NEW_USERNAME);
		if (this.usr == null) {
			this.username = RootUser.ROOT_NAME.equals(cur) ? null : cur;
		} else if (RootUser.ROOT_NAME.equals(cur)) {
			if (!(this.usr instanceof RootUser)) {
				this.usr = this.usr.makeRoot();
			}
		} else {
			this.usr = this.usr.changeName(cur);
		}
	}
	
	private void cmdStatus(List<String> args) {
		if (args.size() == 1) {
			cmdStatusUser();
			cmdStatusWorld();
			cmdStatusServer();
			cmdStatusServerPW();
		} else {
			for (int i = 1; i < args.size(); i++) {
				final String argUser             = "user";               //$NON-NLS-1$
				final String argWorld            = "world";              //$NON-NLS-1$
				final String argWorldRemoteSize  = "world-remote-size";  //$NON-NLS-1$
				final String argWorldRemoteAll   = "world-remote-all";   //$NON-NLS-1$
				final String argWorldRemoteWorld = "world-remote-world"; //$NON-NLS-1$
				final String argServer           = "server";             //$NON-NLS-1$
				final String argServerpw         = "serverpw";           //$NON-NLS-1$
				final String argServerPW         = "server-pw";          //$NON-NLS-1$
				switch (args.get(i).toLowerCase()) {
				case HELP -> {
					this.c.writeLines(
						MessageFormat.format(STATUS_HELP_BLA_BLA, HELP, argUser, argWorldRemoteAll, argWorldRemoteWorld, argServer, argServerpw, argServerPW));
				}
				case argUser -> cmdStatusUser();
				case argWorld -> cmdStatusWorld();
				case argWorldRemoteSize -> cmdStatusWorldRemoteSize();
				case argWorldRemoteAll, argWorldRemoteWorld -> cmdStatusWorldRemoteAll();
				case argServer -> cmdStatusServer();
				case argServerpw, argServerPW -> cmdStatusServerPW();
				default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
				}
			}
		}
	}
	
	private void cmdStatusServerPW() {
		if (this.serverPW != null) {
			this.c.writeLines(STATUS_SERVER_PW_THERE_IS_ONE);
		} else {
			this.c.writeLines(STATUS_SERVER_PW_THERE_IS_NONE);
			if (this.serverThread != null) {
				this.c.writeLines(STATUS_SERVER_PW_NONE_BUT_SERVER_RUNNING);
			}
		}
	}
	
	private void cmdStatusServer() {
		if (this.serverThread != null) {
			Map<User, Connection> cs = this.connects;
			this.c.writeLines(MessageFormat.format(STATUS_SERVER_RUNNING, cs == null ? STATUS_SERVER_NO_CONNECTS : Integer.toString(cs.size())));
		} else {
			this.c.writeLines(STATUS_SERVER_NO_SERVER);
		}
	}
	
	private void cmdStatusWorldRemoteAll() {
		if (this.world == null || !(this.world instanceof RemoteWorld rw)) {
			this.c.writeLines(STATUS_MISSING_REMOTE_WORLD);
			return;
		}
		try {
			rw.updateWorld();
			this.c.writeLines(STATUS_REMOTE_WORLD_SIZES_UPDATED);
			this.c.writeLines(MessageFormat.format(STATUS_BOUNDS_XLEN_0_YLEN_1, Integer.toString(this.world.xlen()), Integer.toString(this.world.ylen())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorldRemoteSize() {
		if (this.world == null || !(this.world instanceof RemoteWorld rw)) {
			this.c.writeLines(STATUS_MISSING_REMOTE_WORLD);
			return;
		}
		try {
			rw.updateWorldSize();
			this.c.writeLines(STATUS_REMOTE_WORLD_SIZES_UPDATED);
			this.c.writeLines(MessageFormat.format(STATUS_BOUNDS_XLEN_0_YLEN_1, Integer.toString(this.world.xlen()), Integer.toString(this.world.ylen())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorld() {
		boolean writeBounds = true;
		if (this.world == null) {
			this.c.writeLines(STATUS_WORLD_NO_WORLD);
			writeBounds = false;
		} else if (this.world instanceof RootWorld) {
			this.c.writeLines(STATUS_WORLD_ROOT_WORLD_LOADED);
		} else if (this.world instanceof RootWorld.Builder) {
			this.c.writeLines(STATUS_WORLD_BUILDER_WORLD_LOADED);
		} else if (this.world instanceof RemoteWorld rw) {
			this.c.writeLines(STATUS_REMOTE_WORLD_SIZES_UPDATED);
			if (!rw.loadedBounds()) {
				this.c.writeLines(STATUS_BOUNDS_NOT_LOADED);
				writeBounds = false;
			}
		} else if (this.world instanceof UserWorld) {
			this.c.writeLines(STATUS_WORLD_USER_WORLD_LOADED);
		} else {
			this.c.writeLines(MessageFormat.format(STATUS_WORLD_UNKNOWN_TYPE_0, this.world.getClass()));
		}
		if (writeBounds) {
			this.c.writeLines(MessageFormat.format(STATUS_BOUNDS_XLEN_0_YLEN_1, Integer.toString(this.world.xlen()), Integer.toString(this.world.ylen())));
		}
	}
	
	private void cmdStatusUser() {
		if (this.usr == null) {
			this.c.writeLines(MessageFormat.format(STATUS_USER_NOT_LOGGED_IN_USERNAME_0, this.username == null ? RootUser.ROOT_NAME : this.username));
		} else {
			this.c.writeLines(MessageFormat.format(STATUS_USER_NAME_0, this.usr.name()));
		}
	}
	
	private void cmdVersion(List<String> args) {
		if (args.size() == 1) {
			this.c.writeLines(MessageFormat.format(VERSION_SQUARE_CONQUERER_VERSION_0, Settings.VERSION_STRING));
		} else if (args.size() == 2 && HELP.equalsIgnoreCase(args.get(1))) {
			this.c.writeLines(MessageFormat.format(VERSION_HELP_0_BLA_BLA, HELP));
		} else {
			this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(2)));
		}
	}
	
	private void cmdHelp(List<String> args) {
		switch (args.size()) {
		case 1 -> {
			this.c.writeLines(MessageFormat.format(HELP_GENERAL_MESSAGE, Settings.VERSION_STRING, CMD_HELP, CMD_VERSION, CMD_STATUS, CMD_USERNAME, CMD_WORLD,
				CMD_SERVER, CMD_SETPW, CMD_SERVERPW, CMD_QUIT, CMD_EXIT, CMD_QUIT, HELP, CMD_HELP));
		}
		case 2 -> {
			if (CMD_HELP.equalsIgnoreCase(args.get(1))) {
				this.c.writeLines(HELP_HELP_MESSAGE);
			} else {
				args.set(0, args.get(1));
				args.set(1, HELP);
				exec(args);
			}
		}
		default -> this.c.writeLines(HELP_TOO_MANY_ARGS);
		}
	}
	
	private char ask(String prompt, String validChars) {
		while (true) {
			String line = this.c.readLine(prompt).trim();
			if (line.isEmpty()) continue;
			char character = line.charAt(0);
			if (validChars.indexOf(character) == -1) continue;
			return character;
		}
	}
	
	private String prompt() {
		if (this.usr != null) {
			return "[" + this.usr.name() + "]: "; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (this.username != null) {
			return "(" + this.username + "): "; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return "(" + RootUser.ROOT_NAME + "): "; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void doTasks() throws AssertionError {
		while (!this.tasks.isEmpty()) {
			Object task = this.tasks.remove(0);
			if (task == null) {
				continue;
			}
			try {
				if (task instanceof Path loadFile) {
					doTaskLoadFile(loadFile);
				} else if (task instanceof StartServerTask sst) {
					doTaskStartServer(sst);
				} else if (task instanceof ConnectToServerTask cst) {
					doTaskConnectToServer(cst);
				} else {
					throw new AssertionError("task has an unknown class: " + task.getClass().getName()); //$NON-NLS-1$ this should never happen
				}
			} catch (RuntimeException e) {
				System.err.println(MessageFormat.format(ERROR_WHILE_EXECUTING_TASK_0, e));
			}
		}
	}
	
	private void doTaskConnectToServer(ConnectToServerTask cst) {
		boolean askUsr = this.usr != null;
		boolean askUN  = this.username != null;
		do {
			if (askUsr) {
				if (askUN) {
					this.username = this.c.readLine(USERNAME_ENTER_YOUR_NEW_USERNAME);
				}
				char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
				this.usr = User.create(this.username, pw);
			}
			if (this.usr != null) {
				try {
					Connection conn;
					if (this.serverPW != null) {
						conn = Connection.ClientConnect.connectNew(cst.host, cst.port, this.usr, this.serverPW);
					} else {
						conn = Connection.ClientConnect.connect(cst.host, cst.port, this.usr);
					}
					this.world = new RemoteWorld(conn);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_CONNECT_ERROR_WHILE_CONNECTING_0, e));
				}
			}
		} while (retry(this.world == null));
	}
	
	private void doTaskStartServer(StartServerTask sst) {
		if (this.world == null) {
			boolean load;
			while (true) {
				String line = this.c.readLine(PROMPT_L_OAD_WORLD_OR_CREATE_A_N_EW_WORLD).trim();
				if (line.isEmpty()) {
					continue;
				}
				switch (line.charAt(0)) {
				case 'l', 'L' -> load = true;
				case 'n', 'N' -> load = false;
				default -> {
					continue;
				}
				}
				break;
			}
			if (load) {
				subTaskStartServerLoadWorld();
			} else {
				subTaskStartServerNewWorld();
			}
		}
		if (this.world != null) {
			synchronized (this) {
				final RootWorld       rw = (RootWorld) this.world;
				Map<User, Connection> cs = new HashMap<>();
				this.connects = cs;
				
				this.serverThread = threadStart(() -> {
					try {
						Connection.ServerAccept.accept(sst.port, rw, (conn, sok) -> {
							if (sok == null) {
								this.c.writeLines(MessageFormat.format(SERVER_USER_0_DISCONNECTED, conn.usr.name()));
							} else {
								this.c.writeLines(MessageFormat.format(SERVER_START_USER_0_LOGGED_IN_FROM_1, conn.usr.name(), sok.getInetAddress()));
							}
						}, cs, this.serverPW);
					} catch (IOException e) {
						this.c.writeLines(SERVER_SERVER_STOPPED_WITH_ERROR + e.toString());
					} finally {
						synchronized (SquareConquererCUI.this) {
							if (this.serverThread == Thread.currentThread()) {
								this.serverThread = null;
								this.connects     = null;
							}
						}
					}
				});
				this.c.writeLines(SERVER_START_STARTED_SERVER);
			}
		}
	}
	
	private void subTaskStartServerNewWorld() {
		int xlen = readNumber(WORLD_NOARG_PROMPT_ENTER_X_LEN, 1, Integer.MAX_VALUE);
		if (xlen > 0) {
			int ylen = readNumber(WORLD_NOARG_PROMPT_ENTER_Y_LEN, 1, Integer.MAX_VALUE);
			if (ylen > 0) {
				RootWorld.Builder b = new RootWorld.Builder((RootUser) this.usr, xlen, ylen);
				b.fillRandom();
				this.world = b.create();
			}
		}
	}
	
	private void subTaskStartServerLoadWorld() {
		boolean askPW = this.usr == null;
		boolean fail;
		do {
			Path loadFile;
			do {
				loadFile = Path.of(this.c.readLine(WORLD_LOAD_PROMPT_ENTER_FILE_TO_BE_LOADED));
				if (!Files.exists(loadFile)) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_THE_FILE_0_DOES_NOT_EXIST, loadFile));
					continue;
				}
				if (!Files.isRegularFile(loadFile)) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_THE_PATH_0_DOES_NOT_A_FILE, loadFile));
					continue;
				}
				break;
			} while (retry(true));
			if (askPW) {
				char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
				this.usr = RootUser.create(pw);
			} else {
				this.usr = this.usr.makeRoot();
			}
			fail = loadFile(loadFile);
		} while (retry(fail));
	}
	
	private void doTaskLoadFile(Path loadFile) {
		boolean askUsr = this.usr == null;
		do {
			if (askUsr) {
				this.c.writeLines(MessageFormat.format(LOAD_TASK_LOAD_NOW_THE_FILE_0, loadFile));
				char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
				if (this.username != null && !RootUser.ROOT_NAME.equals(this.username)) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				this.usr = RootUser.create(pw);
			} else if (!(this.usr instanceof RootUser)) {
				this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				this.usr = this.usr.makeRoot();
			} else { // reset other users
				this.usr = this.usr.makeRoot();
			}
		} while (retry(loadFile(loadFile)));
	}
	
	private int readNumber(String prompt, int min, int max) {
		int val = -1;
		do {
			String line = this.c.readLine(prompt).trim();
			try {
				val = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				this.c.writeLines(MessageFormat.format(READN_NUM_COULD_NOT_PARSE_THE_NUMBER_0_1, line, e.toString()));
			}
		} while (retry(val < min || val > max, MessageFormat.format(READN_NUM_THE_MINIMUM_NUMBER_IS_0_1, Integer.toString(min),
			(max != Integer.MAX_VALUE ? READN_NUM_AND_THE_MAXIMUM_NUMBER_IS + max : "")))); //$NON-NLS-1$
		return val;
	}
	
	private boolean retry(boolean failed) {
		return retry(failed, ASK_RETRY_Y_ES_N_O);
	}
	
	private boolean retry(boolean failed, String errorPrompt) {
		if (!failed) return false;
		while (true) {
			String line = this.c.readLine(errorPrompt).trim();
			if (line.isEmpty()) continue;
			switch (line.charAt(0)) {
			case 'y', 'Y':
				return true;
			case 'n', 'N':
				return false;
			default:
			}
		}
	}
	
	private boolean loadFile(Path loadFile) {
		try (InputStream in = Files.newInputStream(loadFile)) {
			Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr);
			RootUser   root = (RootUser) this.usr;
			root.load(conn);
			Tile[][] tiles = RemoteWorld.loadWorld(conn, root.users());
			this.world    = RootWorld.Builder.createBuilder(root, tiles);
			this.username = null;
			this.c.writeLines(WORLD_LOAD_LOADED_WORLD_AND_USERS);
			this.c.writeLines(WORLD_TO_BUILD_FINISH);
			return false;
		} catch (IOException e) {
			this.usr = null;
			this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
			return true;
		}
	}
	
}
