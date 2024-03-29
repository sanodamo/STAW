USE [JAC]
GO

/****** Object:  Table [dbo].[COOKIES]    Script Date: 3/11/2018 9:17:45 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING OFF
GO

CREATE TABLE [dbo].[COOKIES](
	[ID] [bigint] IDENTITY(1,1) NOT NULL,
	[P_ID] [varchar](255) NOT NULL,
	[TSTAMP] [timestamp] NOT NULL,
	[NAME] [varchar](255) NOT NULL,
	[DOMAIN] [varchar](255) NOT NULL,
	[VALUE] [varchar](255) NOT NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

